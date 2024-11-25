
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 出入
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/churu")
public class ChuruController {
    private static final Logger logger = LoggerFactory.getLogger(ChuruController.class);

    private static final String TABLE_NAME = "churu";

    @Autowired
    private ChuruService churuService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private BaoxiuService baoxiuService;//报修
    @Autowired
    private CheweiService cheweiService;//车位
    @Autowired
    private CheweiFenpeiService cheweiFenpeiService;//车位分配
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private FangwuService fangwuService;//房屋
    @Autowired
    private FeiyongService feiyongService;//物业费缴纳
    @Autowired
    private GonggaoService gonggaoService;//公告
    @Autowired
    private LiuyanService liuyanService;//物业人员投诉
    @Autowired
    private SixinService sixinService;//我的私信
    @Autowired
    private WuyeService wuyeService;//物业人员
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("物业人员".equals(role))
            params.put("wuyeId",request.getSession().getAttribute("userId"));
        params.put("churuDeleteStart",1);params.put("churuDeleteEnd",1);
        CommonUtil.checkMap(params);
        PageUtils page = churuService.queryPage(params);

        //字典表数据转换
        List<ChuruView> list =(List<ChuruView>)page.getList();
        for(ChuruView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ChuruEntity churu = churuService.selectById(id);
        if(churu !=null){
            //entity转view
            ChuruView view = new ChuruView();
            BeanUtils.copyProperties( churu , view );//把实体数据重构到view中
            //级联表 用户
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(churu.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody ChuruEntity churu, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,churu:{}",this.getClass().getName(),churu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            churu.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<ChuruEntity> queryWrapper = new EntityWrapper<ChuruEntity>()
            .eq("yonghu_id", churu.getYonghuId())
            .eq("churu_name", churu.getChuruName())
            .eq("churu_types", churu.getChuruTypes())
            .eq("churu_delete", churu.getChuruDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ChuruEntity churuEntity = churuService.selectOne(queryWrapper);
        if(churuEntity==null){
            churu.setChuruDelete(1);
            churu.setInsertTime(new Date());
            churu.setCreateTime(new Date());
            churuService.insert(churu);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ChuruEntity churu, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,churu:{}",this.getClass().getName(),churu.toString());
        ChuruEntity oldChuruEntity = churuService.selectById(churu.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            churu.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

            churuService.updateById(churu);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<ChuruEntity> oldChuruList =churuService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        ArrayList<ChuruEntity> list = new ArrayList<>();
        for(Integer id:ids){
            ChuruEntity churuEntity = new ChuruEntity();
            churuEntity.setId(id);
            churuEntity.setChuruDelete(2);
            list.add(churuEntity);
        }
        if(list != null && list.size() >0){
            churuService.updateBatchById(list);
        }

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<ChuruEntity> churuList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            ChuruEntity churuEntity = new ChuruEntity();
//                            churuEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            churuEntity.setChuruName(data.get(0));                    //出入名称 要改的
//                            churuEntity.setChuruTypes(Integer.valueOf(data.get(0)));   //出入类型 要改的
//                            churuEntity.setChuruTime(sdf.parse(data.get(0)));          //出入时间 要改的
//                            churuEntity.setChuruContent("");//详情和图片
//                            churuEntity.setChuruDelete(1);//逻辑删除字段
//                            churuEntity.setInsertTime(date);//时间
//                            churuEntity.setCreateTime(date);//时间
                            churuList.add(churuEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        churuService.insertBatch(churuList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = churuService.queryPage(params);

        //字典表数据转换
        List<ChuruView> list =(List<ChuruView>)page.getList();
        for(ChuruView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ChuruEntity churu = churuService.selectById(id);
            if(churu !=null){


                //entity转view
                ChuruView view = new ChuruView();
                BeanUtils.copyProperties( churu , view );//把实体数据重构到view中

                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(churu.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody ChuruEntity churu, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,churu:{}",this.getClass().getName(),churu.toString());
        Wrapper<ChuruEntity> queryWrapper = new EntityWrapper<ChuruEntity>()
            .eq("yonghu_id", churu.getYonghuId())
            .eq("churu_name", churu.getChuruName())
            .eq("churu_types", churu.getChuruTypes())
            .eq("churu_delete", churu.getChuruDelete())
//            .notIn("churu_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ChuruEntity churuEntity = churuService.selectOne(queryWrapper);
        if(churuEntity==null){
            churu.setChuruDelete(1);
            churu.setInsertTime(new Date());
            churu.setCreateTime(new Date());
        churuService.insert(churu);

            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

}
