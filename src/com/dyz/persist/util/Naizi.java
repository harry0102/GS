package com.dyz.persist.util;

/**
 * Created by kevin on 2016/6/29.
 */
public class Naizi {
//    private int[] paiList = new int[]{1, 0, 0, 0, 1, 1, 1, 1, 1,    0, 0, 0, 0, 0, 1, 2, 2, 1,    0, 0, 0, 0, 0, 0, 0, 0, 0,2};
//
//    public static void main(String[] args){
//        Naizi test = new Naizi();
//        System.out.println(Naizi.testHuiPai(test.paiList));
//    }

    public static  boolean testHuiPai(int [] paiList ){
       return getNeedHunNum(paiList);
    }

    /**
     * 得到胡牌需要的赖子数
     * type 将在哪里
     * @return
     */
    private static boolean getNeedHunNum(int[] paiList){
    	int zhong = paiList[27];
        int[] wan_arr = new int[9];
        int[] tiao_arr = new int[9];
        int[] tong_arr = new int[9];
        int needNum = 0;
        int index = 0;
        for(int i=0;i<27;i++){
            if(i<9){
                wan_arr[index] = paiList[i];
                index++;
            }
            if(i>=9 && i<18){
                if(i == 9){
                    index = 0;
                }
                tiao_arr[index] = paiList[i];
                index++;
            }
            if(i>=18){
                if(i == 18){
                    index = 0;
                }
                tong_arr[index] = paiList[i];
                index++;
            }
        }

        needNum = getNumWithJiang(wan_arr.clone()) + getNumber(tiao_arr.clone()) + getNumber(tong_arr.clone());
        if(needNum <= zhong){
        	//System.out.println("将在万中 要  "+needNum +" wan = "+getNumWithJiang(wan_arr.clone())+"   tiao = "+getNumber(tiao_arr.clone())+"    tong = "+getNumber(tong_arr.clone()));
        	return true;
        }
        else {
        	needNum = getNumber(wan_arr.clone()) + getNumWithJiang(tiao_arr.clone()) + getNumber(tong_arr.clone());
        	if(needNum <= zhong){
        		// System.out.println("将在条中 要  "+needNum+" wan = "+getNumber(wan_arr.clone())+"   tiao = "+getNumWithJiang(tiao_arr.clone())+"    tong = "+getNumber(tong_arr.clone()));
        		return true;
        	}
        	else{
        		needNum = getNumber(wan_arr.clone()) + getNumber(tiao_arr.clone()) + getNumWithJiang(tong_arr.clone());
        		if(needNum <= zhong){
        			return true;
        			//System.out.println("将在筒中 要  "+needNum+" wan = "+getNumber(wan_arr.clone())+"   tiao = "+getNumber(tiao_arr.clone())+"    tong = "+getNumWithJiang(tong_arr.clone()));
        		}
        		else{
        			return false;
        		}
        	}
        }
    }

    private static int getNumWithJiang(int[] temp_arr){
        boolean isjiang = false;
        int result = 0;
        for(int i=0;i<9;i++){
            if(temp_arr[i]== 3) {
                temp_arr[i] = 0;//先去除掉成坎的牌组
            }
            if(temp_arr[i] == 4){
                temp_arr[i] = 1;//这4张牌还在手中的情况
            }else if(temp_arr[i] > 4) {
                temp_arr[i] = 0;//该牌被扛掉了
            }
        }
        for(int i=0;i<9;i++){
            if(temp_arr[i]>0){
                    if (i < 7) {
                        if(temp_arr[i] >= 2 && isjiang == false && temp_arr[i+1] == 0){//先判断有将牌没有，如果没有将牌，先将这两张牌作将
                            temp_arr[i] -= 2;
                            isjiang = true;
                            i--;
                        }else {
                            // 如果 这张牌的下一张和再下一张都不为空的情况。可以组成一级牌
                            if (temp_arr[i + 1] > 0 && temp_arr[i + 2] > 0) {
                                temp_arr[i]--;
                                temp_arr[i + 1]--;
                                temp_arr[i + 2]--;
                                i--;
                            } else if (temp_arr[i + 1] > 0 && temp_arr[i + 2] == 0) {
                                //如果这张牌的下一张不为空，再下一张为空，需要一张赖子
                                temp_arr[i]--;
                                temp_arr[i + 1]--;
                                result++;
                                i--;
                            } else if (temp_arr[i + 1] == 0 && temp_arr[i + 2] > 0) {
                                //如果下一张为空，再下一张不为空，先判断有将没有，如果没有将，并且这张牌只有一张，补一张赖子组成将
                                //                                                             如果这张牌有两张以上，直接做将，不补赖子。
                                //                                                如果有将的情况，为中间补一张赖子。
                                if (isjiang == false) {
                                    if (temp_arr[i] == 1) {
                                        temp_arr[i] = 0;
                                        isjiang = true;
                                        result++;
                                        i--;
                                    } else {
                                        if (temp_arr[i] >= 2) {
                                            isjiang = true;
                                            temp_arr[i] -= 2;
                                            i--;
                                        }
                                    }
                                } else {
                                    temp_arr[i]--;
                                    temp_arr[i + 2]--;
                                    result++;
                                    i--;
                                }
                            } else if (temp_arr[i + 1] == 0 && temp_arr[i + 2] == 0) {
                                //如果下一张和再下一张牌都为空的情况，先判断有将没得，如果有将了，为下一张牌补一张赖子
                                //                                                    如果还没有将，并且这张牌只有一张，补一张赖子组成将
                                //                                                                  如果这张牌有两张以上，直接做将，不补赖子。
                                if (isjiang == true) {
                                    temp_arr[i + 1]++;
                                    result++;
                                    i--;
                                } else {
                                    if (temp_arr[i] == 1) {
                                        temp_arr[i] = 0;
                                        isjiang = true;
                                        result++;
                                        i--;
                                    } else {
                                        if (temp_arr[i] >= 2) {
                                            isjiang = true;
                                            temp_arr[i] -= 2;
                                            i--;
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        //牌面点子大得7的时候，先判断有没有将牌，如果没有，并且这张牌只有一张，补一张赖子组成将
                        //                                                 如果这张牌有两张以上，直接做将，不补赖子。
                        //                                      如果有将牌，则判断当前牌和下一张牌是否为空，如果都不为空，在前面补一张赖子
                        //                                                  如果下一张牌为空，则将当前牌补成坎，缺多少张补多少张赖子
                        if(i == 7){
                            if(isjiang == false){
                                if(temp_arr[i] == 1){
                                    temp_arr[i] = 0;
                                    result++;
                                    isjiang = true;
                                }else if (temp_arr[i] >= 2){
                                    temp_arr[i] -=  2;
                                    isjiang = true;
                                    i--;
                                }
                            }else {
                                if (temp_arr[i] > 0 && temp_arr[i + 1] > 0) {
                                    temp_arr[i]--;
                                    temp_arr[i+1]--;
                                    result++;
                                    i--;
                                } else if (temp_arr[i] > 0 && temp_arr[i + 1] == 0) {
                                    result = result + 3 - temp_arr[i];
                                    temp_arr[i] = 0;
                                }
                            }
                        }else{
                            if(isjiang == false) {
                                if(temp_arr[i] == 1){
                                    temp_arr[i] = 0;
                                    result++;
                                    isjiang = true;
                                }else if (temp_arr[i] >= 2){
                                    temp_arr[i] -=  2;
                                    isjiang = true;
                                    i--;
                                }
                            }else{
                                //如果当前牌为9点时，直接补成坎
                                result = result + 3 - temp_arr[i];
                                temp_arr[i] = 0;
                            }
                        }
                    }
            }
        }

        if(isjiang == false){
            result += 2;
        }
        System.out.print("getNumWithJiang");
        for(int a = 0;a<temp_arr.length;a++){
            System.out.print(temp_arr[a]+",");
        }
        System.out.println();
        return  result;
    }

    private static int getNumber(int[] temp_arr){
        int result = 0;
        for(int i=0;i<9;i++) {
            if(temp_arr[i] > 0) {
                if (i < 7) {
                    if (temp_arr[i + 1] > 0 && temp_arr[i + 2] > 0) {
                        temp_arr[i]--;
                        temp_arr[i + 1]--;
                        temp_arr[i + 2]--;
                        i--;
                    } else if (temp_arr[i + 1] > 0 && temp_arr[i + 2] == 0) {
                        temp_arr[i]--;
                        temp_arr[i + 1]--;
                        result++;
                        i--;
                    } else if (temp_arr[i + 1] == 0 && temp_arr[i + 2] > 0) {
                        temp_arr[i]--;
                        temp_arr[i + 2]--;
                        result++;
                        i--;
                    } else if (temp_arr[i + 1] == 0 && temp_arr[i + 2] == 0) {
                        if (temp_arr[i] == 2) {
                            temp_arr[i] = 0;
                            result++;
                        } else {
                            temp_arr[i] = 0;
                        }
                    }
                } else {
                    if(i == 7) {
                        if (temp_arr[i] > 0 && temp_arr[i + 1] > 0) {
                            temp_arr[i]--;
                            temp_arr[i]--;
                            result++;
                            i--;
                        } else if (temp_arr[i] > 0 && temp_arr[i + 1] == 0) {
                            result = result + 3 - temp_arr[i];
                            temp_arr[i] = 0;
                        }
                    }else{
                        result  = result + 3-temp_arr[i];
                        temp_arr[i] = 0;
                    }
                }
            }
        }

        System.out.print("getNumber");
        for(int a = 0;a<temp_arr.length;a++){
            System.out.print(temp_arr[a]+",");
        }
        System.out.println();
        return result;
    }
}
