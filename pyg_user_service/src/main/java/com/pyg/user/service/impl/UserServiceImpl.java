package com.pyg.user.service.impl;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbUserMapper;
import com.pyg.pojo.TbUser;
import com.pyg.pojo.TbUserExample;
import com.pyg.pojo.TbUserExample.Criteria;
import com.pyg.user.service.UserService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private Destination queueSmsDestination;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {

		//1.将密码加密
		user.setPassword (DigestUtils.md5Hex (user.getPassword ()));
		//2.增加创建时间，修改时间的内容
		user.setCreated (new Date ());
		user.setUpdated (new Date ());
		userMapper.insert(user);

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}
			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	//通过jmsTemplate发送验证码
	@Override
	public void sendCode(String phone) {
		//发送的数据是手机号和验证码（随机）
		String randomNumeric = RandomStringUtils.randomNumeric (6);
		System.out.println ("随机验证码为"+randomNumeric);
		//将验证码保存到redis中，用来与用户输入的验证码进行比较,用手机号当大key
		redisTemplate.boundValueOps (phone).set (randomNumeric,1, TimeUnit.DAYS);
		//发送消息
		jmsTemplate.send (queueSmsDestination, new MessageCreator () {
			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage mapMessage=  session.createMapMessage ();
				mapMessage.setString ("phoneNumber",phone);
				mapMessage.setString ("param","{'code':'"+randomNumeric+"'}");
				return mapMessage;
			}
		});
	}
	//校验验证码
	@Override
	public boolean checkSmscode(String smscode, String phone) {
		String rancode = (String) redisTemplate.boundValueOps (phone).get ();
		if (rancode==null){
			return false;}
		if (!rancode.equals (smscode)){
			return false;
		}
		return true;
	}

}
