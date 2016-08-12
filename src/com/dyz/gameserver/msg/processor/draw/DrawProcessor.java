package com.dyz.gameserver.msg.processor.draw;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.draw.DrawResponse;
import com.dyz.myBatis.model.Prize;
import com.dyz.myBatis.services.PrizeService;
import com.dyz.persist.util.GlobalUtil;
import com.dyz.persist.util.PrizeProbability;

/**
 * 抽奖处理类
 * @author luck
 *
 */
public class DrawProcessor extends MsgProcessor implements
INotAuthProcessor  {

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		if(GlobalUtil.checkIsLogin(gameSession)) {
			String type = request.getString();//传入类型 0：获取所有奖品信息    1：获取随机获得奖品id    
			Avatar avatar = gameSession.getRole(Avatar.class);
			if (avatar != null) {
				if(type.equals("0")){
					//获取所有奖品信息
					prizesInformation(gameSession);
				}
				else if(type.equals("1")){
					//随机获取奖品id
					getPrizeInfo(gameSession);
				}
			}
			else{
				System.out.println("账户信息有误");
			}
		}
		else{
			System.out.println("账户未登录");
		}
	}	
	/**
	 * 返回所有的抽奖奖品信息
	 */
	public void  prizesInformation(GameSession gameSession){
		//type      0：获取奖品信息    1：获取随机获得奖品id   
		//PrizeService.getInstance().selectByPrimaryKey(1);
		JSONObject json  = new JSONObject();
		try {
			List<Prize>  list = PrizeService.getInstance().selectAllPrizes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		json.put("data", PrizeService.getInstance().selectAllPrizes());
		json.put("type", "0");
		gameSession.sendMsg(new DrawResponse(1,json));
	}
	/**
	 * 抽取随机奖品，并返回id
	 */
	public void getPrizeInfo(GameSession gameSession){
		//type    0：获取奖品信息    1：获取随机获得奖品id   
		//取随机数 0-9999  数组下标对应的数就是奖品在表中的id
		JSONObject json  = new JSONObject();
		int randomNum = (int)Math.floor(Math.random()*9999);
		json.put("data", PrizeProbability.prizeList.get(randomNum));
		json.put("type", "1");
		gameSession.sendMsg(new DrawResponse(1,json));
	}
}