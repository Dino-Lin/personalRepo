package com.dino.hibernate.manager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.dino.hibernate.Condition;
import com.dino.hibernate.OrderBy;
import com.dino.hibernate.Updater;
import com.dino.hibernate.page.Pagination;


public interface HibernateBaseManager<T extends Serializable> {
	/**
	 * 通过ID查找对象
	 * 
	 * @param id
	 *            记录的ID
	 * @return 实体对象
	 */
	public T findById(Serializable id);

	public T load(Serializable id);

	/**
	 * 查找所有对象
	 * 
	 * @return 对象列表
	 */
	public List<T> findAll();
	
	public int countAll();
	public int countAllNotDel();
	public int countByPoroperty(String property, Object value);
	public int countByProperties(Map<String, Object> properties);

	public Pagination findAll(int pageNo, int pageSize, OrderBy... orderBys);

	/**
	 * 通过示例对象查找对象列表
	 * 
	 * @param eg
	 *            示例对象
	 * @param anyWhere
	 *            是否模糊查询。默认false。
	 * @param conds
	 *            排序及is null。分别为OrderBy和String。
	 * @param exclude
	 *            排除属性
	 * @return
	 */
	public List<T> findByEgList(T eg, boolean anyWhere, Condition[] conds, String... exclude);

	public List<T> findByEgList(T eg, boolean anyWhere, String... exclude);

	public List<T> findByEgList(T eg, Condition[] conds, String... exclude);

	public List<T> findByEgList(T eg, boolean anyWhere, Condition[] conds, int firstResult, int maxResult,
			String... exclude);

	public List<T> findByEgList(T eg, boolean anyWhere, int firstResult, int maxResult, String... exclude);

	public List<T> findByEgList(T eg, Condition[] conds, int firstResult, int maxResult, String... exclude);

	public List<T> findByEgList(T eg, String... exclude);

	public Pagination findByEg(T eg, boolean anyWhere, Condition[] conds, int pageNo, int pageSize, String... exclude);

	public Pagination findByEg(T eg, boolean anyWhere, int pageNo, int pageSize, String... exclude);

	public Pagination findByEg(T eg, Condition[] conds, int pageNo, int pageSize, String... exclude);

	public Pagination findByEg(T eg, int pageNo, int pageSize, String... exclude);

	/**
	 * 根据Updater更新对象
	 * 
	 * @param updater
	 */
	public Object updateByUpdater(Updater updater);

	public Object updateDefault(Object entity);

	/**
	 * 保存对象
	 * 
	 * @param entity
	 *            实体对象
	 * @return 操作信息
	 */
	public T save(T entity);

	public Object update(Object o);

	public Object saveOrUpdate(Object o);

	public void delete(Object o);

	/**
	 * 根据ID删除记录
	 * 
	 * @param id
	 *            记录ID
	 */
	public T deleteById(Serializable id);

	/**
	 * 根据ID数组删除记录，当发生异常时，操作终止并回滚
	 * 
	 * @param ids
	 *            记录ID数组
	 * @return 删除的对象
	 */
	public List<T> deleteById(Serializable[] ids);

	public List<T> deleteById2(Serializable[] ids2);
	/**
	 * 保存并刷新对象，避免many-to-one属性不完整
	 * 
	 * @param entity
	 */
	public T saveAndRefresh(T entity);
	/**
	 * 根据对象属性查询所有列表 
	 * 
	 * @param property
	 *            属性名
	 * @param value
	 *            属性值
	 * @return 列表
	 */
	public List<T> findByProperty(String property, Object value);
	/**
	 * 按多组属性查找对象列表.
	 */
	public List<T> findByProperties(Map<String, Object> properties);
	
	/**
	 * 按属性查找唯一对象.
	 */
	public T findUniqueByProperty(String property, Object value) ;
}
