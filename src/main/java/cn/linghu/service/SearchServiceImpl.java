package cn.linghu.service;

import cn.linghu.pojo.ResultModel;
import cn.linghu.pojo.Sku;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class SearchServiceImpl implements SearchService {

    //每页查询20条数据
    public final static Integer PAGE_SIZE = 20;

    @Override
    public ResultModel query(String queryString, String price, Integer page) throws Exception {
        long startTime = System.currentTimeMillis();

        //1. 需要使用的对象封装
        ResultModel resultModel = new ResultModel();
        //从第几条开始查询
        int start = (page - 1) * PAGE_SIZE;
        //查询到多少条为止
        Integer end = page * PAGE_SIZE;
        //创建分词器
        Analyzer analyzer = new IKAnalyzer();
        //创建组合查询对象
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        //2. 根据查询关键字封装查询对象
        QueryParser queryParser = new QueryParser("name", analyzer);
        Query query1 = null;
        //判断传入的查询关键字是否为空, 如果为空查询所有, 如果不为空, 则根据关键字查询
        if (StringUtils.isEmpty(queryString)) {
            query1 = queryParser.parse("*:*");
        } else {
            query1 = queryParser.parse(queryString);
        }
        //将关键字查询对象, 封装到组合查询对象中
        builder.add(query1, BooleanClause.Occur.MUST);

        //3. 根据价格范围封装查询对象
        if (!StringUtils.isEmpty(price)) {
            String[] split = price.split("-");
            Query query2 = IntPoint.newRangeQuery("price", Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            //将价格查询对象, 封装到组合查询对象中
            builder.add(query2, BooleanClause.Occur.MUST);
        }

        //4. 创建Directory目录对象, 指定索引库的位置
        /**
         * 使用MMapDirectory消耗的查询时间
         * ====消耗时间为=========324ms
         * ====消耗时间为=========18ms
         */
        Directory directory = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建输入流对象
        IndexReader reader = DirectoryReader.open(directory);
        //6. 创建搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        //7. 搜索并获取搜索结果
        TopDocs topDocs = indexSearcher.search(builder.build(), end);
        //8. 获取查询到的总条数
        resultModel.setRecordCount(topDocs.totalHits);
        //9. 获取查询到的结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        long endTime = System.currentTimeMillis();
        System.out.println("====消耗时间为=========" + (endTime - startTime) + "ms");

        //10. 遍历结果集封装返回的数据
        List<Sku> skuList = new ArrayList<>();
        if (scoreDocs != null) {
            for (int i = start; i < end; i ++) {
                //通过查询到的文档编号, 找到对应的文档对象
                Document document = reader.document(scoreDocs[i].doc);
                //封装Sku对象
                Sku sku = new Sku();
                sku.setId(document.get("id"));
                sku.setPrice(Integer.parseInt(document.get("price")));
                sku.setImage(document.get("image"));
                sku.setName(document.get("name"));
                sku.setBrandName(document.get("brandName"));
                sku.setCategoryName(document.get("categoryName"));

                skuList.add(sku);
            }
        }
        //封装查询到的结果集
        resultModel.setSkuList(skuList);
        //封装当前页
        resultModel.setCurPage(page);
        //总页数
        Long pageCount = topDocs.totalHits % PAGE_SIZE > 0 ? (topDocs.totalHits/PAGE_SIZE) + 1 : topDocs.totalHits/PAGE_SIZE;
        resultModel.setPageCount(pageCount);
        return resultModel;
    }
}
