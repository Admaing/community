package com.newcoder.community.entity;

/**
 * 封装分页信息
 */
public class Page {
//    当前的页码 默认也是第一页
    private int current = 1;
//    显示的上限
    private int limit = 10;

    //    自己查的 数据总数(用于计算总的页数)
    private int rows;
    //    查询路径 （首页，末页第几页）复用分页连接
    private String path;
    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current>=1){
            this.current = current;
        }

    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit>=1&&limit<=100){
            this.limit = limit;
        }

    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows>=0){
            this.rows = rows;
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

//获取当前页的起始行
    public int getOffset(){
        //current * limit - limit
        return (current-1)*limit;
    }
//    用来获取总的页数
    public int getTotal(){
//        rows/limit 【+1】
        if (rows%limit==0){
            return rows/limit;
        }else{
            return rows/limit+1;
        }
    }
//    从第几页到第几页
// 生成开始页面
    public int getFrom(){
        int from = current-2;
        return from<1?1:from;
    }

    public int getTo(){
        int to = current+2;
        int total = getTotal();
        return to>total?total:to;
    }

}
