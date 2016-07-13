package com.dyz.gameserver.msg.processor.pass;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import net.sf.json.JSONObject;

/**
 * Created by kevin on 2016/7/5.
 */
public class GaveUpMsgProcessor extends MsgProcessor implements
        INotAuthProcessor {
    @Override
    public void process(GameSession gameSession, ClientRequest request) throws Exception {
        RoomLogic roomLogic = RoomManager.getInstance().getRoom(gameSession.getRole(Avatar.class).roomVO.getRoomId());
        JSONObject json = JSONObject.fromObject(request.getString());
        int passType =  Integer.parseInt(json.get("passType").toString());//pass的类型(1-胡，2-杠，3-碰，4-吃)
        if(roomLogic != null){
            roomLogic.gaveUpAction(gameSession.getRole(Avatar.class),passType);
        }
    }
}
