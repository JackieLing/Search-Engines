package cn.linghu.service;

import cn.linghu.pojo.ResultModel;
import cn.linghu.pojo.ResultModel;

/**
 *
 */
public interface SearchService {

    public ResultModel query(String queryString, String price, Integer page) throws Exception;
}
