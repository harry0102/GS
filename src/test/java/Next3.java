package test.java;

import com.context.ConnectAPI;
import com.dyz.gameserver.pojo.CardVO;
import com.dyz.gameserver.pojo.LoginVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.persist.util.JsonUtilTool;

//import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;


public class Next3 {
    public static final String IP_ADDR = "localhost";//服务器地址
    public static final int PORT = 10122;//服务器端口号
    public static int[][] pa = new int[2][42];
    public static int avatarIndex = 4;

    public static void main(String[] args) {
        System.out.println("客户端启动...");
        System.out.println("当接收到服务器端字符为 \"OK\" 的时候, 客户端将终止\n");
        if (args.length != 1) {
            System.out.println("闲家启动需带上*房间号*");
            return;
        }
        while (true) {
            Socket socket = null;
            try {
                //创建一个流套接字并将其连接到指定主机上的指定端口号
                socket = new Socket(IP_ADDR, PORT);

                //读取服务器端数据
                DataInputStream input = new DataInputStream(socket.getInputStream());
                //向服务器端发送数据
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                //玩家121，123，124，125
                String str = "125";

                System.out.println("--125 登陆开始...");

                LoginVO loginVO1 = new LoginVO();
                loginVO1.setOpenId(str);
                //登录操作，不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
                ClientSendRequest loginSend = new ClientSendRequest(ConnectAPI.LOGIN_REQUEST);
                loginSend.output.writeUTF(JsonUtilTool.toJson(loginVO1));
                out.write(loginSend.entireMsg().array());//
                serverCallBack(input);

                System.out.println("--125 登陆结束...");

                System.out.println("--125 加入房间开始, roomId = " + args[0]);

                //加入房间测试
				RoomVO roomVo = new RoomVO();
				roomVo.setRoomId(Integer.parseInt(args[0]));
				ClientSendRequest joinRoomSend = new ClientSendRequest(ConnectAPI.JOIN_ROOM_REQUEST);
				joinRoomSend.output.writeUTF(JsonUtilTool.toJson(roomVo));
				out.write(joinRoomSend.entireMsg().array());//
				serverCallBack(input);//返回317824

                System.out.println("--125 加入房间开始结束...");


                System.out.println("--125 准备游戏开始...");
                //游戏开始测试
                ClientSendRequest gameStartSend = new ClientSendRequest(ConnectAPI.PrepareGame_MSG_REQUEST);
                JSONObject pldInfo = new JSONObject();
                pldInfo.put("run", false);
                pldInfo.put("dunla", false);
                gameStartSend.output.writeUTF(pldInfo.toString());
                out.write(gameStartSend.entireMsg().array());//
                serverCallBack(input);//返回317824

                System.out.println("--125 准备游戏结束...");

                /*
                0-8 ：1-9万
                9-17：1-9条
                18-26：1-9筒
                27-33：东南西北中发白
                以上都是四张的牌）

                34-41：春夏秋冬梅兰竹菊， （各一张）就是这样的索引啊
                 */

                Scanner in = new Scanner(System.in);
                char c;
                boolean flag;
                boolean bQuit = false;

                //x 1 表示 出牌 2万
                //p 1 表示 碰牌 2万
                //g 1 表示 杠牌 2万
                //c 1 表示 吃牌 2万, 同时带上 2,3,表示三四万吃2万。c cardpoint onepoint twopoint
                //h 1 表示 胡牌 2万
                //i 1 表示 更新手里牌，入了张2万 （手动方便控制）
                //d 1 表示 更新手里牌，去了张2万 （手动方便控制）
                //m 表示 摸牌
                //f 表示 放弃
                //t 表示 听牌
                //r 表示 读取服务器返回信息

                while(!bQuit) {
                    System.out.print("请输入指令:\t");
                    String s = in.nextLine();
                    if (s.length() == 0) {
                        s = "r";
                    }
                    s = s.trim();
                    String[] ss = s.split(" ");
                    c = ss[0].charAt(0);
                    flag = false;

                    switch (c) {
                        case 'x':
                            int cardPoint = Pai.getCardPoint(ss[1]);
                            CardVO cardVO = new CardVO();
                            cardVO.setCardPoint(cardPoint);
                            ClientSendRequest rq = new ClientSendRequest(ConnectAPI.CHUPAI_REQUEST);
                            rq.output.writeUTF(JsonUtilTool.toJson(cardVO));
                            out.write(rq.entireMsg().array());
                            flag = true;
                            break;
                        case 'p':
                            int pp = Pai.getCardPoint(ss[1]);
                            CardVO pco = new CardVO();
                            pco.setCardPoint(pp);
                            ClientSendRequest pq = new ClientSendRequest(ConnectAPI.PENGPAI_REQUEST);
                            pq.output.writeUTF(JsonUtilTool.toJson(pco));
                            out.write(pq.entireMsg().array());
                            flag = true;
                            break;
                        case 'g':
                            int gp = Pai.getCardPoint(ss[1]);
                            JSONObject gj = new JSONObject();
                            gj.put("cardPoint", gp);
                            gj.put("gangType", 1); //sounds useless
                            ClientSendRequest gq = new ClientSendRequest(ConnectAPI.GANGPAI_REQUEST);
                            gq.output.writeUTF(gj.toString());
                            out.write(gq.entireMsg().array());
                            flag = true;
                            break;
                        case 'c':
                            int cp = Pai.getCardPoint(ss[1]);
                            int p1 = Pai.getCardPoint(ss[2]);
                            int p2 = Pai.getCardPoint(ss[3]);
                            CardVO co = new CardVO();
                            co.setCardPoint(cp);
                            co.setOnePoint(p1);
                            co.setTwoPoint(p2);
                            ClientSendRequest cq = new ClientSendRequest(ConnectAPI.CHIPAI_REQUEST);
                            cq.output.writeUTF(JsonUtilTool.toJson(co));
                            out.write(cq.entireMsg().array());
                            flag = true;
                            break;
                        case 'm':
                            ClientSendRequest mq = new ClientSendRequest(ConnectAPI.PICKCARD_REQUEST);
                            out.write(mq.entireMsg().array());
                            flag = true;
                            break;
                        case 'f':
                            ClientSendRequest fq = new ClientSendRequest(ConnectAPI.GAVEUP_REQUEST);
                            out.write(fq.entireMsg().array());
                            flag = true;
                            break;
                        case 'h':
                            int hp = Pai.getCardPoint(ss[1]);;
                            CardVO hco = new CardVO();
                            hco.setCardPoint(hp);
                            hco.setType("ss");
                            ClientSendRequest hq = new ClientSendRequest(ConnectAPI.HUPAI_REQUEST);
                            hq.output.writeUTF(JsonUtilTool.toJson(hco));
                            out.write(hq.entireMsg().array());
                            flag = true;
                            break;
                        case 't':
                            ClientSendRequest tq = new ClientSendRequest(ConnectAPI.TINGPAI_REQUEST);
                            JSONObject tj = new JSONObject();
                            tj.put("ting", true);
                            tq.output.writeUTF(tj.toString());
                            out.write(tq.entireMsg().array());
                            flag = true;
                            break;
                        case 'r':
                            serverCallBack(input);
                            break;
                        case 'i':
                            updateCard(Pai.getCardPoint(ss[1]), false);
                            break;
                        case 'd':
                            updateCard(Pai.getCardPoint(ss[1]), true);
                            break;
                        default:
                            System.out.println("\t***输入操作指令错误***");
                            break;
                    }

                    if (flag) {
                        serverCallBack(input);
                    }

                    bQuit = s.equals("quit");
                }

                out.close();
                input.close();
            } catch (Exception e) {
                System.out.println("客户端异常:" + e.getMessage());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        socket = null;
                        System.out.println("客户端 finally 异常:" + e.getMessage());
                    }
                }
            }
            break;
        }
    }

    public static void serverCallBack(DataInputStream input){
        try {
            byte byte1 = input.readByte();
            int len = input.readInt();
            int code = input.readInt();//msgCode
            int status = input.readInt();
            String ret = input.readUTF();

            if (ret.length() > 0) {
                if (code != ConnectAPI.headRESPONSE) {
                    System.out.printf("Got server response: protocol code = 0x%06x, status = %d\n", code, status);
                    System.out.println("\tdata = " + ret);

                    if (ret.indexOf("paiArray") > 0) {
                        JSONObject json = JSONObject.fromObject(ret);
                        JSONArray paiArray = (JSONArray) json.get("paiArray");
                        if (paiArray != null && paiArray.size() == 2) {
                            JSONArray a1 = paiArray.getJSONArray(0);
                            JSONArray a2 = paiArray.getJSONArray(1);

                            System.out.println("\tpai = " + a1);
                            System.out.println("\tsize = " + a1.size());

                            for (int i = 0; i < a1.size(); i++) {
                                pa[0][i] = (int) a1.get(i);
                                pa[1][i] = (int) a2.get(i);
                            }

                            System.out.println("起手牌");
                            Pai.printCards(pa, avatarIndex);
                        }
                    } else {
                        serverCallBack(input);
                    }
                }
            }

            // 如接收到 "OK" 则断开连接
            if ("OK".equals(ret)) {
                System.out.println("客户端将关闭连接");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateCard(int cardIdx, boolean rm){
        if (rm) {
            pa[0][cardIdx]--;
        } else {
            pa[0][cardIdx]++;
        }
        Pai.printCards(pa, avatarIndex);
    }

}  