package com.dyz.gameserver.net;

import com.dyz.gameserver.bootstrap.GameServer;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 前段
 * @author Administrator
 *
 */
public class MinaMsgHandler extends IoHandlerAdapter{
	
	private static final Logger logger = LoggerFactory.getLogger(MinaMsgHandler.class);
	
//	@Override
//	public void sessionCreated(IoSession session) throws Exception {
//
//	}
	@Override
	public void sessionOpened(IoSession session) throws Exception{
		new GameSession(session);
		logger.info("a session create from ip {}",session.getRemoteAddress());

	}
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		ClientRequest clientRequest = (ClientRequest) message;
		GameSession gameSession = GameSession.getInstance(session);
		if (gameSession == null) {
			logger.info("gameSession == null");
			return;
		}
		
		GameServer.msgDispatcher.dispatchMsg(gameSession,clientRequest);
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		//强制退出
		logger.error("服务器出错 {}",cause.getMessage());
		cause.printStackTrace();
	}

	/**
	 * 关闭SESSION
	 * @param session
	 * @throws Exception
     */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("a session closed ip:{}",session.getRemoteAddress());
		GameSession gameSession = GameSession.getInstance(session);
		if(gameSession!=null){
			//解散房间
			//每次有人退出房间/退出游戏就检测看房间还剩余几人，若只剩余一个，则解散房间
			/*RoomVO roomvo = gameSession.getRole(Avatar.class).getRoomVO();
			if( roomvo != null){
				Avatar ava = gameSession.getRole(Avatar.class);
				if(roomvo.getPlayerList().size() <= 2){
					System.out.println("解散房间：房间号："+gameSession.getRole(Avatar.class).getRoomVO().getRoomId());
					gameSession.getRole(Avatar.class).setRoomVO(new RoomVO());
				}
				else{
					//int avatarIndex = roomvo.getPlayerList().indexOf(ava);
					roomvo.getPlayerList().remove(ava.avatarVO);
					RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomvo.getRoomId());
					if(roomLogic != null) {
						for (int i = 0; i < roomLogic.getPlayerList().size(); i++) {
							if(roomLogic.getPlayerList().get(i).getUuId() == ava.getUuId()){
								roomLogic.getPlayerList().remove(ava);
								i--;
							}else {
								roomLogic.getPlayerList().get(i).getSession().sendMsg(new ServerResponse(101, 1));
							}
						}
					}
				}
			}*/
			gameSession.close();
		}

	}
	
	
}
