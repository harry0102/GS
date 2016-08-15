package com.dyz.persist.util;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.context.GameServerContext;
import com.dyz.gameserver.sprite.tool.AsyncTaskQueue;
import com.dyz.myBatis.model.Account;
import com.dyz.myBatis.services.AccountService;

import java.util.*;

/**
 * Created by kevin on 2016/8/15.
 * 每天固定时间执行任务的定时器
 */
public class TaskTimer {
    static int count = 0;
    AsyncTaskQueue asyncTaskQueue = new AsyncTaskQueue();
    public static void showTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ++count;
                System.out.println("时间=" + new Date() + " 执行了" + count + "次"); // 1次
                List<Account> accounts = AccountService.getInstance().selectAll();
                if(accounts != null) {
                    for (int i = 0; i < accounts.size(); i++) {
                        Avatar tempAva = GameServerContext.getAvatarFromOn(accounts.get(i).getUuid());
                        if(tempAva != null){
                            tempAva.avatarVO.getAccount().setPrizecount(1);
                            //当前用户在用，要不要通知增加了抽奖次数？
                        }else{
                            tempAva =  GameServerContext.getAvatarFromOff(accounts.get(i).getUuid());
                            if(tempAva != null){
                                tempAva.avatarVO.getAccount().setPrizecount(1);
                            }
                        }
                        Account temp = new Account();
                        temp.setId(accounts.get(i).getId());
                        temp.setPrizecount(1);
                        AccountService.getInstance().updateByPrimaryKeySelective(temp);
                    }
                }
            }
        };

        //设置执行时间
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);//每天
        //定制每天的21:09:00执行，
        calendar.set(year, month, day, 23, 59, 59);
        Date date = calendar.getTime();
        Timer timer = new Timer();
        System.out.println(date);
        timer.schedule(task, date,24*60*60*1000);
    }
}
