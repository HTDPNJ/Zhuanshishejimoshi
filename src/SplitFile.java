import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SplitFile
{
    //文件的路径
    private String filePath;
    //文件名
    private String fileName;
    //文件大小
    private long length;
    //块数
    private int size;
    //每块的大小
    private long blockSize;
    //分割后的存放目录
    private String destBlockPath;
    //每块的名称
    private List<String> blockPath;

    public SplitFile()
    {
        blockPath=new ArrayList<String>();
    }
    public SplitFile(String filePath,String destBlockPath)
    {
        this(filePath,1024,destBlockPath);
    }
    public SplitFile(String filePath, long blockSize,String destBlockPath)
    {
        this();
        this.filePath=filePath;
        this.destBlockPath=destBlockPath;
        this.blockSize=blockSize;
        init();
    }
    /*初始化操作 计算块数、确定文件名*/
    public void init(){
        File src =null;
        if(null==filePath|| !((src=new File(filePath)).exists())){
            return; //文件不存在，返回
        }
        if(src.isDirectory()){
            return; //文件夹不能分割
        }
        //文件名
        this.fileName=src.getName();

        //计算块数 实际大小 与每块大小
        this.length=src.length();
        if(this.blockSize>length){
            this.blockSize=length;
        }
        //确定块数
        size=(int)(Math.ceil(length*1.0/this.blockSize));
        initPathName();
    }
    private void initPathName(){
        for(int i=0;i<size;i++){
            this.blockPath.add(destBlockPath+"/"+this.fileName+".part"+i);
        }
    }
    /*文件的分割
     0、第几块
    * 1、起始位置
    * 2、实际大小*/
    public void split(String destPath){
        long beginPos=0;//起始点
        long actualBlockSize=blockSize; //实际大小
        //计算所有块的大小、位置、索引
        for(int i=0;i<size;i++){
            if(i==size-1){ //最后一块
                actualBlockSize=this.length-beginPos;
            }
            splitDetail(i,beginPos,actualBlockSize);
            beginPos+=actualBlockSize; //本次的终点，下次的起点
        }
    }
    private void splitDetail(int idx,long beginPos,long actualBlockSize){
        //1、创建源
        File src=new File(this.filePath); //源文件
        File dest=new File(this.blockPath.get(idx)); //目标文件
        //2、选择流
        RandomAccessFile raf=null; //输入流
        BufferedOutputStream bos=null; //输出流
        try {
            raf = new RandomAccessFile(src, "r");
            bos = new BufferedOutputStream(new FileOutputStream(dest));
            //读取文件
            raf.seek(beginPos);
            //缓冲区
            byte[] flush = new byte[1024];
            int len = 0;
            while (-1 != (len = raf.read(flush))) { //查看是否足够写出
                if(actualBlockSize-len>=0){
                    bos.write(flush,0,len);
                    actualBlockSize-=len;
                }else{ //写出最后一次的剩余量
                    bos.write(flush,0,(int)actualBlockSize);
                    break;
                }
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                bos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                raf.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*文件的合并*/
    public void mergeFile(String destPath){
        File dest=new File(destPath);
        BufferedOutputStream bos=null;
        //输出流

        try {
            bos=new BufferedOutputStream(new FileOutputStream(dest,true));
            BufferedInputStream bis=null;
            for (int i = 0; i < this.blockPath.size(); i++) {
                bis = new BufferedInputStream(new FileInputStream(new File(this.blockPath.get(i))));
               //缓冲区
                byte[] flush=new byte[1024];
                //接收长度
                int len=0;
                while(-1!=(len=bis.read(flush))){
                    bos.write(flush,0,len);
                }
                bos.flush();
                bis.close();
            }
        }catch (Exception e){
        }finally {
            try {
                bos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /*文件的合并*/
    public void mergeFile2(String destPath){
        File dest=new File(destPath);
        BufferedOutputStream bos=null;//输出流
        SequenceInputStream sis=null;//输入流

        //创建一个容器
        Vector<InputStream> vi=new Vector<InputStream>();
        try {
            for (int i = 0; i < this.blockPath.size(); i++) {
                vi.add(new BufferedInputStream(new FileInputStream(new File(this.blockPath.get(i)))));
            }
            bos=new BufferedOutputStream(new FileOutputStream(dest,true));
            sis=new SequenceInputStream(vi.elements());
            //缓冲区
            byte[] flush=new byte[1024];
            //接收长度
            int len=0;
            while(-1!=(len=sis.read(flush))){
                bos.write(flush,0,len);
            }
            bos.flush();
            sis.close();
        }catch (Exception e){
        }finally {
            try {
                bos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    public static void main(String[] args)
    {
        SplitFile file=new SplitFile("C:/Users/Oliver/Desktop/2.txt",1,"C:/Users/Oliver/Desktop");
        System.out.println(file.size);
        file.split("终点路径");

    }
}
