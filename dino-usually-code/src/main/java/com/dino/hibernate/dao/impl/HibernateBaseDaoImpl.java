package com.dino.hibernate.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Example.PropertySelector;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.dino.common.util.BeanMapUtil;
import com.dino.hibernate.Condition;
import com.dino.hibernate.Finder;
import com.dino.hibernate.Nullable;
import com.dino.hibernate.OrderBy;
import com.dino.hibernate.Updater;
import com.dino.hibernate.dao.HibernateBaseDao;
import com.dino.hibernate.page.Pagination;
import com.dino.hibernate.page.SimplePage;
import com.dino.hibernate.query.PageQuery;

/**
 * DAO基类。
 * 
 * 提供hql分页查询，example分页查询，拷贝更新等功能。
 * 
 * @author liufang
 * 
 * @param <T>
 */
@Repository
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class HibernateBaseDaoImpl<T extends Serializable> implements HibernateBaseDao<T> {// extends
																					// HibernateDaoSupport
	private Logger log = Logger.getLogger(getClass());
	
	@Resource
	private SessionFactory sessionFactory;

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	private final static ThreadLocal<SimplePage> pageContext = new ThreadLocal ();
	
	public T save(T entity) {
		Assert.notNull(entity,"对象不能为空");
		getSession().save(entity);
		return entity;
	}

	public Object update(Object entity) {
		Assert.notNull(entity,"对象不能为空");
		getSession().update(entity);
		return entity;
	}

	public Object saveOrUpdate(Object entity) {
		Assert.notNull(entity,"对象不能为空");
		getSession().saveOrUpdate(entity);
		return entity;
	}

	public Object merge(Object entity) {
		Assert.notNull(entity,"对象不能为空");
		return getSession().merge(entity);
	}

	public void delete(Object entity) {
		Assert.notNull(entity,"对象不能为空");
		getSession().delete(entity);
	}

	public T deleteById(Serializable id) {
		Assert.notNull(id,"id不能为空");
		T entity = load(id);
		getSession().delete(entity);
		return entity;
	}

	public T load(Serializable id) {
		Assert.notNull(id,"id不能为空");
		return load(id, false);
	}

	public T get(Serializable id) {
		Assert.notNull(id,"id不能为空");
		return (T) getSession().get(getPersistentClass(), id);
	}

	public T load(Serializable id, boolean lock) {
		Assert.notNull(id,"id不能为空");
		T entity = null;
		if (lock) {
			entity = (T) getSession().load(getPersistentClass(), id, LockOptions.UPGRADE);
		} else {
			entity = (T) getSession().load(getPersistentClass(), id);
		}
		return entity;
	}

	public List<T> findAll() {
		return findByCriteria();
	}
	public Pagination findAllByPage(){
		return findAllByPage(20);
	}
	public Pagination findAllByPage(Integer pageSize){
		if(pageSize == null || pageSize.intValue() <=0){
			pageSize = 20;
		}
		SimplePage pageInfo =  pageContext.get();
		if(pageInfo == null){
			pageInfo = new SimplePage(1, pageSize, countAll());
			pageContext.set(pageInfo);
		}
		int currentPage = 1;
		if(pageInfo.hasNextPage()){
			currentPage = pageInfo.fetchNextPage();
		}else{
			pageInfo = null;
			pageContext.set(null);
			return null;
		}		
		return this.findAll(currentPage, pageInfo.getPageSize());
		
	}

	public List<T> findAll(OrderBy... orders) {
		Criteria crit = createCriteria();
		if (orders != null) {
			for (OrderBy order : orders) {
				crit.addOrder(order.getOrder());
			}
		}
		return crit.list();
	}

	public Pagination findAll(int pageNo, int pageSize, OrderBy... orders) {
		Criteria crit = createCriteria();
		return findByCriteria(crit, pageNo, pageSize, null, OrderBy.asOrders(orders));
	}

	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param hql
	 *            hql语句
	 * @param values
	 *            数量可变的参数
	 */
	protected List find(String hql, Object... values) {
		return createQuery(hql, values).list();
	}

	/**
	 * 按HQL查询唯一对象.
	 */
	protected Object findUnique(String hql, Object... values) {
		return createQuery(hql, values).uniqueResult();
	}

	/**
	 * 按属性查找对象列表.
	 */
	public List<T> findByProperty(String property, Object value) {
		Assert.hasText(property,"属性不能为空");
		return createCriteria(Restrictions.eq(property, value)).list();
	}
	/**
	 * 按多组属性查找对象列表.
	 */
	public List<T> findByProperties(Map<String, Object> properties) {
		Set entrySet = properties.entrySet();
		List<Criterion> criterionList = new ArrayList<Criterion>();
		for(Object entryObj : entrySet){
			Entry<String, Object> entry = (Entry<String, Object>)entryObj;
			String property = entry.getKey();
			Object value = entry.getValue();
			Assert.hasText(property,"属性不能为空");
			criterionList.add(Restrictions.eq(property, value));
		}
		return createCriteria(criterionList.toArray(new Criterion[0])).list();
	}

	/**
	 * 按属性查找唯一对象.
	 */
	public T findUniqueByProperty(String property, Object value) {
		Assert.hasText(property,"属性不能为空");
		Assert.notNull(value,"对象值不能为空");
		return (T) createCriteria(Restrictions.eq(property, value)).uniqueResult();
	}
	public int countByProperties(Map<String ,Object> properties){
		Set entrySet = properties.entrySet();
		List<Criterion> criterionList = new ArrayList<Criterion>();
		for(Object entryObj : entrySet){
			Entry<String, Object> entry = (Entry<String, Object>)entryObj;
			String property = entry.getKey();
			Object value = entry.getValue();
			Assert.hasText(property,"属性不能为空");
			criterionList.add(Restrictions.eq(property, value));
		}
		return ((Number) (createCriteria(criterionList.toArray(new Criterion[0])).setProjection(Projections.rowCount())
				.uniqueResult())).intValue();
	}

	public int countByProperty(String property, Object value) {
		Assert.hasText(property,"属性不能为空");
		Assert.notNull(value,"对象值不能为空");
		return ((Number) (createCriteria(Restrictions.eq(property, value)).setProjection(Projections.rowCount())
				.uniqueResult())).intValue();
	}
	public int countAll(){
		return ((Number) (createCriteria().setProjection(Projections.rowCount()).uniqueResult())).intValue();
	}

	protected Pagination find(Finder finder, int pageNo, int pageSize) {
		int totalCount = countQueryResult(finder);
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = getSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		List list = query.list();
		p.setList(list);
		return p;
	}
	/***
	 * 统一分页和list返回
	 * @param finder
	 * @param query
	 * @return
	 */
	protected Pagination find(Finder finder, PageQuery query) {
		if(query.getPageSize()==null||query.getPageSize().intValue()==0){
		  List list=find(finder);
		  if(list!=null&&list.size()>0){
			  return new Pagination(1, list.size(), list.size(),list);
		  }else{
			  return new Pagination();
		  }
		}else{
			return find(finder, query.getPageNo(), query.getPageSize());
		}
		
	}
	
	protected Pagination findForExist(Finder finder, int pageNo, int pageSize){
		int totalCount = countQueryResultForExist(finder);
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = getSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		List list = query.list();
		p.setList(list);
		return p;
	}
	
	/**
	 * 自定义from是第几个的查找方式
	 * @param finder
	 * @param pageNo
	 * @param pageSize
	 * @param fromNum
	 * @return
	 */
	protected Pagination findForCustomFromIndex(Finder finder, int pageNo, int pageSize, int fromNum) {
		int totalCount = countQueryResultForCustomFromIndex(finder, fromNum);
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = getSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		List list = query.list();
		p.setList(list);
		return p;
	}
	
	protected Pagination finds(Finder finder, int pageNo, int pageSize) {
		int totalCount = find(finder).size();
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = getSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		List list = query.list();
		p.setList(list);
		return p;
	}

	protected List find(Finder finder) {
		Query query = getSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(finder.getFirstResult());
		if (finder.getMaxResults() > 0) {
			query.setMaxResults(finder.getMaxResults());
		}
		List list = query.list();
		return list;
	}

	public List<T> findByEgList(T eg, boolean anyWhere, Condition[] conds, String... exclude) {
		Criteria crit = getCritByEg(eg, anyWhere, conds, exclude);
		return crit.list();
	}

	public List<T> findByEgList(T eg, boolean anyWhere, Condition[] conds, int firstResult, int maxResult,
			String... exclude) {
		Criteria crit = getCritByEg(eg, anyWhere, conds, exclude);
		crit.setFirstResult(firstResult);
		crit.setMaxResults(maxResult);
		return crit.list();
	}

	public Pagination findByEg(T eg, boolean anyWhere, Condition[] conds, int page, int pageSize, String... exclude) {
		Order[] orderArr = null;
		Condition[] condArr = null;
		if (conds != null && conds.length > 0) {
			List<Order> orderList = new ArrayList<Order>();
			List<Condition> condList = new ArrayList<Condition>();
			for (Condition c : conds) {
				if (c instanceof OrderBy) {
					orderList.add(((OrderBy) c).getOrder());
				} else {
					condList.add(c);
				}
			}
			orderArr = new Order[orderList.size()];
			condArr = new Condition[condList.size()];
			orderArr = orderList.toArray(orderArr);
			condArr = condList.toArray(condArr);
		}
		Criteria crit = getCritByEg(eg, anyWhere, condArr, exclude);
		return findByCriteria(crit, page, pageSize, null, orderArr);
	}

	/**
	 * 根据查询函数与参数列表创建Query对象,后续可进行更多处理,辅助函数.
	 */
	protected Query createQuery(String queryString, Object... values) {
		Assert.hasText(queryString,"属性名不能为空");
		Query queryObject = getSession().createQuery(queryString);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
			}
		}
		return queryObject;
	}

	/**
	 * 按Criterion查询对象列表.
	 * 
	 * @param criterion
	 *            数量可变的Criterion.
	 */
	protected List<T> findByCriteria(Criterion... criterion) {
		return createCriteria(criterion).list();
	}

	protected Pagination findByCriteria(Criteria crit, int pageNo, int pageSize, Projection projection, Order... orders) {
		int totalCount = ((Number) crit.setProjection(Projections.rowCount()).uniqueResult()).intValue();
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		crit.setProjection(projection);
		if (projection == null) {
			crit.setResultTransformer(Criteria.ROOT_ENTITY);
		}
		if (orders != null) {
			for (Order order : orders) {
				crit.addOrder(order);
			}
		}
		crit.setFirstResult(p.getFirstResult());
		crit.setMaxResults(p.getPageSize());
		p.setList(crit.list());
		return p;
	}

	/**
	 * 通过count查询获得本次查询所能获得的对象总数.
	 * 
	 * @param finder
	 * @return
	 */
	protected int countQueryResult(Finder finder) {
		Query query = getSession().createQuery(finder.getRowCountHql());
		finder.setParamsToQuery(query);
		Iterator it=query.iterate();
		if(it.hasNext()){
			int k = ((Number) it.next()).intValue();
			return k;
		}
		return 0;
	}
	
	/**
	 * 通过count查询获得本次查询所能获得的对象总数(exist).
	 * 
	 * @param finder
	 * @return
	 */
	protected int countQueryResultForExist(Finder finder) {
		Query query = getSession().createQuery(finder.getRowCountHqlForExist());
		finder.setParamsToQuery(query);
		int k = ((Number) query.iterate().next()).intValue();
		return k;
	}
	
	/**
	 * 通过count查询获得本次查询所能获得的对象总数(自定义from是语句中的第几个).
	 * 
	 * @param finder
	 * @return
	 */
	protected int countQueryResultForCustomFromIndex(Finder finder, int fromNum) {
		Query query = getSession().createQuery(finder.getRowCountHqlForCustomFromIndex(fromNum));
		finder.setParamsToQuery(query);
		int k = ((Number) query.iterate().next()).intValue();
		return k;
	}
	
	protected int countSQLQueryResultForCustomFromIndex(Finder finder, int fromNum) {
		Query query = getSession().createSQLQuery(finder.getRowCountHqlForCustomFromIndex(fromNum));
		finder.setParamsToQuery(query);
		int k = ((Number) query.list().get(0)).intValue();
		return k;
	}

	/**
	 * 通过count查询获得本次查询所能获得的对象总数.
	 * 
	 * @return page对象中的totalCount属性将赋值.
	 */
	protected int countQueryResult(Criteria c) {
		// 执行Count查询
		int totalCount = (Integer) c.setProjection(Projections.rowCount()).uniqueResult();
		if (totalCount < 1) {
			return 0;
		}
		return totalCount;
	}

	protected Criteria getCritByEg(T bean, boolean anyWhere, Condition[] conds, String... exclude) {
		Criteria crit = getSession().createCriteria(getPersistentClass());
		Example example = Example.create(bean);
		example.setPropertySelector(NOT_BLANK);
		if (anyWhere) {
			example.enableLike(MatchMode.ANYWHERE);
			example.ignoreCase();
		}
		for (String p : exclude) {
			example.excludeProperty(p);
		}
		crit.add(example);
		// 处理排序和is null字段
		if (conds != null) {
			for (Condition o : conds) {
				if (o instanceof OrderBy) {
					OrderBy order = (OrderBy) o;
					crit.addOrder(order.getOrder());
				} else if (o instanceof Nullable) {
					Nullable isNull = (Nullable) o;
					if (isNull.isNull()) {
						crit.add(Restrictions.isNull(isNull.getField()));
					} else {
						crit.add(Restrictions.isNotNull(isNull.getField()));
					}
				} else {
					// never
				}
			}
		}
		// 处理many to one查询
		ClassMetadata cm = getCmd(bean.getClass());
		String[] fieldNames = cm.getPropertyNames();
		for (String field : fieldNames) {
			Object o = cm.getPropertyValue(bean, field);
			if (o == null) {
				continue;
			}
			ClassMetadata subCm = getCmd(o.getClass());
			if (subCm == null) {
				continue;
			}
			Serializable id = subCm.getIdentifier(o,(SessionImplementor) this.getSession());
			if (id != null) {
				Serializable idName = subCm.getIdentifierPropertyName();
				crit.add(Restrictions.eq(field + "." + idName, id));
			} else {
				crit.createCriteria(field).add(Example.create(o));
			}
		}
		return crit;
	}

	public void refresh(Object entity) {
		getSession().refresh(entity);
	}

	public Object updateDefault(Object entity) {
		return updateByUpdater(Updater.create(entity));
	}

	public Object updateByUpdater(Updater updater) {
		ClassMetadata cm = getCmd(updater.getBean().getClass());
		if (cm == null) {
			throw new RuntimeException("所更新的对象没有映射或不是实体对象");
		}
		Object bean = updater.getBean();
		Object po = getSession().load(bean.getClass(), cm.getIdentifier(bean, (SessionImplementor)getSession()));
		updaterCopyToPersistentObject(updater, po);
		return po;
	}

	/**
	 * 将更新对象拷贝至实体对象，并处理many-to-one的更新。
	 * 
	 * @param updater
	 * @param po
	 */
	private void updaterCopyToPersistentObject(Updater updater, Object po) {
		Map map = BeanMapUtil.describe(updater.getBean());
		Set<Map.Entry<String, Object>> set = map.entrySet();
		for (Map.Entry<String, Object> entry : set) {
			String name = entry.getKey();
			Object value = entry.getValue();
			if (!updater.isUpdate(name, value)) {
				continue;
			}
			if (value != null) {
				Class valueClass = value.getClass();
				ClassMetadata cm = getCmd(valueClass);
				if (cm != null) {
					Serializable vid =getSession().getIdentifier(value); cm.getIdentifier(value,  (SessionImplementor)getSession());
					// 如果更新的many to one的对象的id为空，则将many to one设置为null。
					if (vid != null) {
						value = getSession().load(valueClass, vid);
					} else {
						value = null;
					}
				}
			}
			try {
				PropertyUtils.setProperty(po, name, value);
			} catch (Exception e) {
				// never
				log.warn("更新对象时，拷贝属性异常【name="+name+",value="+value+"】", e);
			}
		}
	}

	/**
	 * 根据Criterion条件创建Criteria,后续可进行更多处理,辅助函数.
	 */
	protected Criteria createCriteria(Criterion... criterions) {
		Criteria criteria = getSession().createCriteria(getPersistentClass());
		if(criterions != null){
			for (Criterion c : criterions) {
				criteria.add(c);
			}
		}
		return criteria;
	}

	private Class<T> persistentClass;

	public void setPersistentClass(Class<T> persistentClass) {
		this.persistentClass = persistentClass;
	}

	public HibernateBaseDaoImpl() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	public Class<T> getPersistentClass() {
		return persistentClass;
	}

	public T createNewEntiey() {
		try {
			return getPersistentClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("不能创建实体对象：" + getPersistentClass().getName());
		}
	}

	private ClassMetadata getCmd(Class clazz) {
		return (ClassMetadata) sessionFactory.getClassMetadata(clazz);
	}

	public static final NotBlankPropertySelector NOT_BLANK = new NotBlankPropertySelector();

	/**
	 * 不为空的EXAMPLE属性选择方式
	 * 
	 * @author liufang
	 * 
	 */
	static final class NotBlankPropertySelector implements PropertySelector {
		private static final long serialVersionUID = 1L;

		public boolean include(Object object, String property, Type type) {
			return object != null && !(object instanceof String && StringUtils.isEmpty((String) object));
		}
	}

	protected List findByNamedQuery(final String namedQuery) {
		return getSession().getNamedQuery(namedQuery).list();
	}

	/**
	 * 
	 * @param query
	 *            :查询语句
	 * @param parameterName
	 *            ：查询语句中的预处理字段
	 * @param parametervalue
	 *            ：查询语句中的预处理字段值
	 * @return
	 */
	protected List findByNamedQuery(String namedQuery, String parameterName, Object parameterValue) {
		return getSession().getNamedQuery(namedQuery).setParameter(parameterName, parameterValue).list();
	}

	/**
	 * 
	 * @param query
	 *            :查询语句
	 * @param parameterNames
	 *            ：查询语句中的预处理字段
	 * @param parametervalue
	 *            ：查询语句中的预处理字段值
	 * @return
	 */
	protected List findByNamedQuery(String namedQuery, String[] parameterNames, Object[] parameterValues) {
		Query queryObject = getSession().getNamedQuery(namedQuery);
		for (int i = 0; i < parameterNames.length; i++) {
			queryObject.setParameter(parameterNames[i], parameterValues[i]);
		}
		return queryObject.list();
	}

	// @SuppressWarnings("unchecked")
	// protected Pagination findByNamedQueryPage(String namedQuery, int pageNo,
	// int pageSize) {
	// int totalCount = countQueryResult(finder);
	// Pagination p = new Pagination(pageNo, pageSize, totalCount);
	// if (totalCount < 1) {
	// p.setList(new ArrayList());
	// return p;
	// }
	// Query query = getSession().getNamedQuery(namedQuery);
	// query.setFirstResult(p.getFirstResult());
	// query.setMaxResults(p.getPageSize());
	// List list = query.list();
	// p.setList(list);
	// }
	//
	// /**
	// *
	// * @param query:查询语句
	// * @param parameterName：查询语句中的预处理字段
	// * @param parametervalue：查询语句中的预处理字段值
	// * @return
	// */
	// @SuppressWarnings("unchecked")
	// protected Pagination findByNamedQueryPage(String namedQuery,
	// String parameterName, Object parameterValue, int pageNo, int pageSize) {
	// return getSession().getNamedQuery(namedQuery).setParameter(
	// parameterName, parameterValue).list();
	// }
	//
	// /**
	// *
	// * @param query:查询语句
	// * @param parameterName：查询语句中的预处理字段
	// * @param parametervalue：查询语句中的预处理字段值
	// * @return
	// */
	// @SuppressWarnings("unchecked")
	// protected Pagination findByNamedQueryPage(String namedQuery,
	// String[] parameterName, Object[] parameterValue, int pageNo, int
	// pageSize) {
	// Query queryObject = getSession().getNamedQuery(namedQuery);
	// for (int i = 0; i < parameterName.length; i++) {
	// queryObject.setParameter(parameterName[i], parameterValue[i]);
	// }
	// return queryObject.list();
	// }
	@Override
	public void virtualDelete(Serializable id) {
		// TODO Auto-generated method stub
		String updateHql = "update "+getGenericType(0).getSimpleName()+" set deleteFlag='Y' where id=:id";
		Session session = getSession();
		session.createQuery(updateHql).setParameter("id", id).executeUpdate();
	}

	public Class getGenericType(int index) {
		java.lang.reflect.Type genType = getClass().getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		java.lang.reflect.Type[] params = ((java.lang.reflect.ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			throw new RuntimeException("Index outof bounds");
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class) params[index];
	}
	
	protected Pagination findSQL(Finder finder, int pageNo, int pageSize) {
		int totalCount = countSqlResult(finder);
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = getSession().createSQLQuery(finder.getOrigHql()).setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		List list = query.list();
		p.setList(list);
		return p;
	}
	
	protected Pagination findSQL2(Finder finder, int pageNo, int pageSize, int formNum) {
		int totalCount = countSQLQueryResultForCustomFromIndex(finder, formNum);
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = getSession().createSQLQuery(finder.getOrigHql()).setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		List list = query.list();
		p.setList(list);
		return p;
	}
	
	protected int countSqlResult(Finder finder) {
		String hql = finder.getOrigHql();
		int fromIndex = hql.toLowerCase().lastIndexOf(Finder.FROM);
		hql = hql.substring(fromIndex);
		String rowCountHql = hql.replace(Finder.HQL_FETCH, "");
		int index = rowCountHql.indexOf(Finder.ORDER_BY);
		if (index > 0) {
			rowCountHql = rowCountHql.substring(0, index);
		}
		rowCountHql = Finder.ROW_COUNT + rowCountHql;
		Query query = getSession().createSQLQuery(rowCountHql);
		finder.setParamsToQuery(query);
		int k = ((Number) query.list().get(0)).intValue();
		return k;
	}



}
