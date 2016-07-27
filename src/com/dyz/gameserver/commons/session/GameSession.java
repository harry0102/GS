package com.dyz.gameserver.commons.session;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;

import com.dyz.gameserver.commons.message.ResponseMsg;
import com.dyz.gameserver.sprite.base.GameObj;
/**
 * 游戏中的session回话，封装了mina的session
 * @author dyz
 *
 */
public class GameSession implements GameObj {
	/**
	 * IoSession
	 */
	private IoSession session;
	/**
	 * 用户的服务器地址
	 */
	private String address;
	/**
	 *
	 */
	private Object role;
	
	private boolean isLogin=false;
	
	private static final AttributeKey KEY_PLAYER_SESSION = new AttributeKey(GameSession.class, "player.session");
	
	public GameSession(IoSession session){
		this.session = session;
		this.session.setAttribute(KEY_PLAYER_SESSION, this);
		SocketAddress socketaddress = session.getRemoteAddress();
		InetSocketAddress s = (InetSocketAddress) socketaddress;
		address = s.getAddress().getHostAddress();
		//存当前用户相关的服务器地址
		
	}

	/**
	 * 得到一个GameSession的实例化对象
	 * @param session
	 * @return
     */
	public static GameSession getInstance(IoSession session) {
		Object playerObj = session.getAttribute(KEY_PLAYER_SESSION);
		return (GameSession) playerObj;
	}
	
	/**
	 * 发送消息给客户端
	 * @param msg
	 * @return
	 * @throws InterruptedException 
	 */
	public WriteFuture sendMsg(ResponseMsg msg)  {
		if (session == null || !session.isConnected() || session.isClosing()) {
			return null;
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return session.write(msg);
	}

	/**
	 *
	 * @return
     */
	public String getAddress(){
		return this.address;
	}

	/**
	 *
	 * @param isLogin
     */
	public  void setLogin(boolean isLogin){
		this.isLogin=isLogin;
	}

	/**
	 * 是否登录
	 * @return
     */
	public boolean isLogin(){
		return this.isLogin;
	}

	/**
	 * 保存角色信息
	 * @param obj
     */
	public void setRole(Object obj){
		this.role = obj;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * 得到角色信息
	 */
	public <T> T getRole(Class<T> t){
		return (T)this.role;
	}

	/**
	 * 关闭SESSION
	 */
	public void close(){
		System.out.println("关闭SESSION -- > "+session.getRemoteAddress());
		if(session != null && session.isConnected()) {
			session.close(false);
			System.out.println("关闭SESSION -- >  session.close(false);");
			//关闭session的时候 如果用户还在房间，则踢出用户
			
			
		}
	}

	@Override
	public void destroy() {
		close();
	}
}
