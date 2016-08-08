package com.dyz.gameserver.logic;

import com.alibaba.fastjson.JSONObject;
import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.joinroom.JoinRoomNoice;
import com.dyz.gameserver.msg.response.joinroom.JoinRoomResponse;
import com.dyz.gameserver.msg.response.outroom.DissolveRoomResponse;
import com.dyz.gameserver.msg.response.outroom.OutRoomResponse;
import com.dyz.gameserver.msg.response.startgame.PrepareGameResponse;
import com.dyz.gameserver.msg.response.startgame.StartGameResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.CardVO;
import com.dyz.gameserver.pojo.HuReturnObjectVO;
import com.dyz.gameserver.pojo.RoomVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kevin on 2016/6/18.
 * 房间逻辑
 */
public class RoomLogic {
    private List<Avatar> playerList;
    private boolean isBegin = false;
    private  Avatar createAvator;
    private RoomVO roomVO;
    private PlayCardsLogic playCardsLogic;
    /**
     * //同意解散房间的人数
     */
    private int dissolveCount = 1;
    /**
     * 房间属性 1-为普通房间
     */
    private int roomType = 1;
    /**
     * 是否添加字牌
     */
    private boolean addWordCard = false;
    /**
     * 房间使用次数
     */
    private int count=0;
    public RoomLogic(RoomVO roomVO){
        this.roomVO = roomVO;
        count = roomVO.getRoundNumber();
    }

    /**
     * 创建房间,默认进入装备状态
     * @param avatar
     */
    public void CreateRoom(Avatar avatar){
        createAvator = avatar;
        roomVO.setPlayerList(new ArrayList<AvatarVO>());
        avatar.avatarVO.setIsReady(true);
        playerList = new ArrayList<Avatar>();
        avatar.avatarVO.setMain(true);
        avatar.setRoomVO(roomVO);
        playerList.add(avatar);
        roomVO.getPlayerList().add(avatar.avatarVO);
    }

    /**
     * 进入房间,默认进入准备状态
     * @param avatar
     */
    public boolean intoRoom(Avatar avatar){
        if(playerList.size() == 4){
			try {
				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000011));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
        }else {
            avatar.avatarVO.setMain(false);
            avatar.avatarVO.setIsReady(true);
            avatar.avatarVO.setRoomId(roomVO.getRoomId());//房间号也放入avatarvo中
            avatar.setRoomVO(roomVO);
            noticJoinMess(avatar);//通知房间里面的其他几个玩家
            playerList.add(avatar);
            roomVO.getPlayerList().add(avatar.avatarVO);
            avatar.getSession().sendMsg(new JoinRoomResponse(1, roomVO));
            if(playerList.size() == 4){
            	//当人数4个时自动开始游戏
                //checkCanBeStartGame();当最后一个人加入时，不需要检测其他玩家是否准备(一句结束后开始才需要检测玩家是否准备)
                Timer timer = new Timer();
                TimerTask tt=new TimerTask() {
                    @Override
                    public void run() {
                    	createAvator.updateRoomCard(-1);//开始游戏，减去房主的房卡
                        startGameRound();
                    }
                };
                timer.schedule(tt, 1000);
            }
            return true;
        }
    }
    /**
     * 当有人加入房间且总人数不够4个时，对其他玩家进行通知
     */
    private void noticJoinMess(Avatar avatar){
    	for (int i = 0; i < playerList.size(); i++) {
            playerList.get(i).getSession().sendMsg(new JoinRoomNoice(1,avatar.avatarVO));
		}
    }
    
    /**
     * 检测是否可以开始游戏
     * @throws IOException 
     */
    public void checkCanBeStartGame() throws IOException{
    	System.out.println("检测是否可以开始游戏");
    	if(playerList.size() == 4){
    		//房间里面4个人且都准备好了则开始游戏
    		List<AvatarVO> avatarVos = new ArrayList<>();
    		for(int i=0;i<playerList.size();i++){
    			if(!playerList.get(i).avatarVO.getIsReady()){
    				//还有人没有准备
    				avatarVos.add(playerList.get(i).avatarVO);
    			}
    		}
    		if(avatarVos.size() == 0){
    			if(count <= 0){
    				//房间次数已经为0
    				for (Avatar avatar : playerList) {
    					avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000010));
    				}
    			}else{
    				isBegin = true;
    				//所有人都准备好了
    				System.out.println("所有人都准备好了");
    				startGameRound();
    			}
    		}
    	}
    }

    /**
     * 退出房间
     * @param avatar
     */
    public void exitRoom(Avatar avatar , int roomId){
    	
        
        JSONObject json = new JSONObject();
//		accountName:”名字”//退出房间玩家的名字(为空则表示是通知的自己)
//		status_code:”0”//”0”退出成功，”1” 退出失败
//		mess：”消息”
//      type："0"//1退出房间    1解散房间
        json.put("accountName", avatar.avatarVO.getAccount().getNickname());
        json.put("status_code", "0");
        json.put("uuid", avatar.getUuId());
        
        
        if(avatar.avatarVO.isMain()){
        	//群主退出房间就是解散房间
        	json.put("type", "1");
        	  for (int i= 0 ; i < playerList.size(); i++) {
        			  playerList.get(i).getSession().sendMsg(new OutRoomResponse(1, json.toString()));
        			  roomVO.getPlayerList().remove(playerList.get(i).avatarVO);
        			  playerList.get(i).setRoomVO(new RoomVO());
        			  playerList.get(i).avatarVO.setRoomId(0);
      		}
        	  RoomManager.getInstance().destroyRoom(roomVO);
        	  playerList.clear();
        	  roomVO.setRoomId(0);
        	  roomVO = null;
        }
        else{
        	json.put("type", "0");
      	    //退出房间。通知房间里面的其他玩家
        	for (int i= 0 ; i < playerList.size(); i++) {
        		//通知房间里面的其他玩家
        		playerList.get(i).getSession().sendMsg(new OutRoomResponse(1, json.toString()));
        		playerList.get(i).avatarVO.setRoomId(0);
        		playerList.get(i).setRoomVO(new RoomVO());
        	}
        	playerList.remove(avatar);
        	roomVO.getPlayerList().remove(avatar.avatarVO);
        	//如果该房间里面的人数只有一个人时，解散房间
        	if(playerList.size() == 1){
	        	  json.put("type", "1");
	          	  for (int i= 0 ; i < playerList.size(); i++) {
	          			  playerList.get(i).getSession().sendMsg(new OutRoomResponse(1, json.toString()));
	          			  roomVO.getPlayerList().remove(playerList.get(i).avatarVO);
	          			  playerList.get(i).setRoomVO(new RoomVO());
	          			  playerList.get(i).avatarVO.setRoomId(0);
	        		}
	          	  //销毁房间
	          	  RoomManager.getInstance().destroyRoom(roomVO);
	        	  playerList.clear();
	        	  roomVO.setRoomId(0);
	        	  roomVO = null;
        	}
        }
    }

    /**
     * 申请解散房间
     */
    public void dissolveRoom(Avatar avatar , int roomId , String type){
    	//向其他几个玩家发送解散房间信息  
    	JSONObject json;
    	//为0时表示是申请解散房间，1表示同意解散房间  2表示不同意解散房间
    	//dissolveCount  = playerList.size();
    	if(type.equals("0")){
    		dissolveCount = 1;
    		json = new JSONObject();
    		json.put("type", "0");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//申请解散房间
    		for (Avatar ava : playerList) {
    			if(ava.getUuId() != avatar.getUuId()){
    				ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    			}
    		}
    	}
    	else if(type.equals("2")){
    		json = new JSONObject();
    		json.put("type", "2");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//拒绝解散房间，向其他玩家发送消息
    		for (Avatar ava : playerList) {
    			if(ava.getUuId() != avatar.getUuId()){
    				ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    			}
    		}
    	}
    	else if(type.equals("1")){
    		//同意解散房间
    		dissolveCount = dissolveCount+1;
    	}
    	//下面是判断是否所有人都同意解散房间
    	int onlineCount = 0;
    	for (Avatar avat : playerList) {
			if(avat.avatarVO.getIsOnLine()){
				onlineCount++;
			}
		}
    	if(onlineCount <= dissolveCount){
    		json = new JSONObject();
    		json.put("type", "1");
    		//所有人都同意了解散房间
    		for (Avatar avat : playerList) {
    			avat.getSession().sendMsg(new DissolveRoomResponse(1, json.toString()));
    			avat.avatarVO.setRoomId(0);;
    		}
/*    		for (Avatar av : playerList) {
    			 av.avatarVO.setRoomId(0);
    			 av.setRoomVO(new RoomVO());
    		     //playerList.remove(av);
    		     //roomVO.getPlayerList().remove(av.avatarVO);
    		    // roomVO = null;
			}
*/    		 
    		 playerList.clear();;
		     roomVO.getPlayerList().clear();
		     roomVO = null;
    	}
    }
    /**
     * 玩家选择放弃操作
     * @param avatar
     * @param  //1-胡，2-杠，3-碰，4-吃
     */
    public void gaveUpAction(Avatar avatar){
        playCardsLogic.gaveUpAction(avatar);
    }

    /**
     * 出牌
     * @return
     */
    public void chuCard(Avatar avatar, int cardIndex){
        playCardsLogic.putOffCard(avatar,cardIndex);
    }

    /**
     * 摸牌
     */
    public void pickCard(){
    	
        playCardsLogic.pickCard();
    }
    /**
     * 吃牌
     * @param avatar
     * @return
     */
    public boolean chiCard(Avatar avatar,CardVO cardVo){
    	return playCardsLogic.chiCard(avatar,cardVo);
    }
    /**
     * 碰牌
     * @param avatar
     * @return
     */
    public boolean pengCard(Avatar avatar,int cardIndex){
    	return playCardsLogic.pengCard( avatar, cardIndex);
    }
    /**
     * 杠牌
     * @param avatar
     * @return
     */
    public boolean gangCard(Avatar avatar,int cardPoint,int gangType){
    	return playCardsLogic.gangCard( avatar, cardPoint,gangType);
    }
    /**
     * 胡牌
     * @param avatar
     * @return
     */
    public boolean huPai(Avatar avatar,int cardIndex,String type){
    	return playCardsLogic.huPai( avatar, cardIndex,type);
    	
    }
    
    /**
     * 游戏准备
     * @param avatar
     * @throws IOException 
     */
    public void readyGame(Avatar avatar) throws IOException{
        if(avatar.avatarVO.getRoomId() != roomVO.getRoomId()){
            System.out.println("你不是这个房间的");
            try {
                avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000006));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        avatar.avatarVO.setIsReady(true);
        int avatarIndex = playerList.indexOf(avatar);
        //成功则返回
        for (Avatar ava : playerList) {
        	ava.getSession().sendMsg(new PrepareGameResponse(1,avatarIndex));
		}
        checkCanBeStartGame();
    }
    /**
     * 开始一回合新的游戏
     */
    private void startGameRound(){
       
         if(count <= 0){
            //房间次数用完了,通知所有玩家
        	for (Avatar avatar : playerList) {
        		try {
					avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000010) );
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        	
        }else{
	        count--;
	        if((count +1) != roomVO.getRoundNumber()){
	        	//说明不是第一局
	        	Avatar avatar = playCardsLogic.bankerAvatar;
	        	playCardsLogic = new PlayCardsLogic();
	        	playCardsLogic.bankerAvatar = avatar;
	        }
	        else{
	        	playCardsLogic = new PlayCardsLogic();
	        }
	        playCardsLogic.setPlayerList(playerList);
	        playCardsLogic.initCard(roomVO);
	        System.out.println("下局开始时人数："+playerList.size());
	        //创建房间信息，游戏记录信息
			//RoomInfoService.getInstance().createRoomInfo(roomVO);
					
	        
	       /* int count = 0;
	        if(roomVO.getRoomType() ==1){
	        	//划水麻将，发了牌之后剩余的牌数量
	        	if(roomVO.getHong()){
	        		count = 4*28-13*4-1;
	        	}
	        	else{
	        		count = 4*27-13*4-1;
	        	}
	        }
	        else if(roomVO.getRoomType() ==2){
	        	//划水麻将
	        	
	        }
	        else{
	        	//长沙麻将
	        	
	        }*/
	        //playCardsLogic.updateSurplusCardCount(count);
	        for(int i=0;i<playerList.size();i++){
	        	//清除各种数据  1：本局胡牌时返回信息组成对象 ，
	        	playerList.get(i).avatarVO.setHuReturnObjectVO(new HuReturnObjectVO());
	            playerList.get(i).getSession().sendMsg(new StartGameResponse(1,playerList.get(i).avatarVO.getPaiArray(),playerList.indexOf(playCardsLogic.bankerAvatar)));
	        }
        }
    }
    
    /**
     * 前后端握手消息处理
     * @param avatar
     */
    public void shakeHandsMsg(Avatar avatar){
    	playCardsLogic.shakeHandsMsg(avatar);
    }
    /**
     * 开始下一局前，玩家准备
     * @param avatar
     */
    public void readyNext(Avatar avatar){
    	playerList.get(playerList.indexOf(avatar)).avatarVO.setIsReady(true);
    	int hasReady = 0;
    	for (Avatar ava : playerList) {
			if(ava.avatarVO.getIsReady()){
				hasReady++;
			}
		}
    	if(hasReady == 4){
    		//如果四家人都准备好了
    		startGameRound();
    	}
    }
    

    public RoomVO getRoomVO() {
        return roomVO;
    }

	public List<Avatar> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<Avatar> playerList) {
		this.playerList = playerList;
	}

	public int getCount() {
		return count;
	}
	
	
}
