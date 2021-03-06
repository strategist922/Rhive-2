package client;

// cc BatchSameRowExample Example application using batch operations, modifying the same row
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.util.Bytes;
import util.HBaseHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * 批量处理同一行的数据例子。
 * @author ia
 *
 */
public class BatchSameRowExample {

  private final static byte[] ROW1 = Bytes.toBytes("row1");
  private final static byte[] COLFAM1 = Bytes.toBytes("colfam1");
  private final static byte[] QUAL1 = Bytes.toBytes("qual1");

  public static void main(String[] args) throws IOException, InterruptedException {
    //获取系统默认配置信息。
	Configuration conf = HBaseConfiguration.create();
    HBaseHelper helper = HBaseHelper.getHelper(conf);
    //为确保系统中不存在这张表。
    helper.dropTable("testtable");
    //创建表”testable“，该表有一个列簇 ”colfam1“ ;
    helper.createTable("testtable" , "colfam1");
    //插入数据，列名：qual1 ,行名：row1 , 值
    helper.put("testtable", "row1" , "colfam1", "qual1", 1L, "val1");
    helper.put("testtable", "row2", "colfam1", "qual1",  1L,  "val2");
    System.out.println("Before batch call...");
    //批量输出“testtable”的“row1”行。
    System.out.println("...............................批量输出结果.................................");
    helper.dump("testtable", new String[] { "row1" } , null, null);
    
    HTable table = new HTable(conf, "testtable");

    // vv BatchSameRowExample
    List<Row> batch = new ArrayList<Row>();

    Put put = new Put(ROW1);
    put.add(COLFAM1, QUAL1, 2L, Bytes.toBytes("val2"));
    //将插入操作添加到批处理过程。
    batch.add(put);
    
    Get get1 = new Get(ROW1);
    get1.addColumn(COLFAM1, QUAL1);
    //将获取数据添加到批处理过程中。
    batch.add(get1);

    Delete delete = new Delete(ROW1);
    delete.deleteColumns(COLFAM1, QUAL1, 3L); // co BatchSameRowExample-1-AddDelete Delete the row that was just put above.
    //将删除列的操作添加到批处理过程中。
    batch.add(delete);

    Get get2 = new Get(ROW1);
    get1.addColumn(COLFAM1, QUAL1);
    //将增加列添加到批处理过程中。
    batch.add(get2);
    
    Object[] results = new Object[batch.size()];
    try {
      table.batch(batch, results);
    } catch (Exception e) {
      System.err.println("Error: " + e);
    }
    // ^^ BatchSameRowExample
    System.out.println("...批量操作...");
    for (int i = 0; i < results.length; i++) {
      System.out.println("Result[" + i + "]: " + results[i]);
    }
    System.out.println("After batch call...");
    helper.dump("testtable", new String[]{ "row1" }, null, null);
  }
}
