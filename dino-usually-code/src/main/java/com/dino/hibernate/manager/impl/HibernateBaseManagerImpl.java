package com.dino.hibernate.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.dino.hibernate.Condition;
import com.dino.hibernate.OrderBy;
import com.dino.hibernate.Updater;
import com.dino.hibernate.dao.HibernateBaseDao;
import com.dino.hibernate.manager.HibernateBaseManager;
import com.dino.hibernate.page.Pagination;

@Transactional
public class HibernateBaseManagerImpl<T extends Serializable> implements HibernateBaseManager<T> {

	private HibernateBaseDao<T> dao;

	public void setDao(HibernateBaseDao<T> dao) {
		this.dao = dao;
	}

	protected HibernateBaseDao<T> getDao() {
		return this.dao;
	}

	@Transactional(readOnly = true)
	public T findById(Serializable id) {
		return dao.get(id);
	}

	@Transactional(readOnly = true)
	public T load(Serializable id) {
		return dao.load(id);
	}

	@Transactional(readOnly = true)
	public List<T> findAll() {
		return dao.findAll();
	}

	@Transactional(readOnly = true)
	public Pagination findAll(int pageNo, int pageSize, OrderBy... orders) {
		return dao.findAll(pageNo, pageSize, orders);
	}

	/**
	 * 实例查找返回列表
	 */

	@Transactional(readOnly = true)
	public List<T> findByEgList(T eg, boolean anywhere, Condition[] conds, String... exclude) {
		return dao.findByEgList(eg, anywhere, conds, exclude);
	}

	@Transactional(readOnly = true)
	public List<T> findByEgList(T eg, boolean anywhere, String... exclude) {
		return this.findByEgList(eg, anywhere, null, exclude);
	}

	@Transactional(readOnly = true)
	public List<T> findByEgList(T eg, Condition[] conds, String... exclude) {
		return this.findByEgList(eg, false, conds, exclude);
	}

	@Transactional(readOnly = true)
	public List<T> findByEgList(T eg, boolean anywhere, Condition[] conds, int firstResult, int maxResult,
			String... exclude) {
		return dao.findByEgList(eg, anywhere, conds, firstResult, maxResult, exclude);
	}

	@Transactional(readOnly = true)
	public List<T> findByEgList(T eg, boolean anywhere, int firstResult, int maxResult, String... exclude) {
		return this.findByEgList(eg, anywhere, null, firstResult, maxResult, exclude);
	}

	@Transactional(readOnly = true)
	public List<T> findByEgList(T eg, Condition[] conds, int firstResult, int maxResult, String... exclude) {
		return this.findByEgList(eg, false, conds, firstResult, maxResult, exclude);
	}

	@Transactional(readOnly = true)
	public List<T> findByEgList(T eg, String... exclude) {
		return this.findByEgList(eg, false, null, exclude);
	}

	@Transactional(readOnly = true)
	public Pagination findByEg(T eg, boolean anywhere, Condition[] conds, int pageNo, int pageSize, String... exclude) {
		return dao.findByEg(eg, anywhere, conds, pageNo, pageSize, exclude);
	}

	@Transactional(readOnly = true)
	public Pagination findByEg(T eg, boolean anywhere, int pageNo, int pageSize, String... exclude) {
		return this.findByEg(eg, anywhere, null, pageNo, pageSize, exclude);
	}

	@Transactional(readOnly = true)
	public Pagination findByEg(T eg, Condition[] conds, int pageNo, int pageSize, String... exclude) {
		return this.findByEg(eg, false, conds, pageNo, pageSize, exclude);
	}

	@Transactional(readOnly = true)
	public Pagination findByEg(T eg, int pageNo, int pageSize, String... exclude) {
		return this.findByEg(eg, false, null, pageNo, pageSize, exclude);
	}

	public Object updateByUpdater(Updater updater) {
		return dao.updateByUpdater(updater);
	}

	public Object updateDefault(Object entity) {
		return updateByUpdater(Updater.create(entity));
	}

	public T save(T entity) {
		return dao.save(entity);
	}

	public T saveAndRefresh(T entity) {
		this.save(entity);
		dao.refresh(entity);
		return entity;
	}

	public Object saveOrUpdate(Object o) {
		return dao.saveOrUpdate(o);
	}

	public void delete(Object o) {
		dao.delete(o);
	}

	public Object update(Object o) {
		return dao.update(o);
	}

	public Object merge(Object o) {
		return dao.merge(o);
	}

	public T deleteById(Serializable id) {
		if (id == null) {
			return null;
		}
		return dao.deleteById(id);
	}

	public List<T> deleteById(Serializable[] ids) {
		List<T> dts = new ArrayList<T>();
		T del = null;
		if (ids != null && ids.length > 0) {
			for (Serializable id : ids) {
				del = deleteById(id);
				if (del != null) {
					dts.add(del);
				}
			}
		}
		return dts;
	}

	public List<T> deleteById2(Serializable[] ids2) {
		List<T> dts = new ArrayList<T>();
		T del = null;
		if (ids2 != null && ids2.length > 0) {
			for (Serializable id : ids2) {
				del = deleteById(id);
				if (del != null) {
					dts.add(del);
				}
			}
		}
		return dts;
	}

	public List<T> findByProperty(String property, Object value) {
		return dao.findByProperty(property, value);
	}

	/**
	 * 按多组属性查找对象列表.
	 */
	public List<T> findByProperties(Map<String, Object> properties) {
		return dao.findByProperties(properties);
	}

	@Override
	public int countAll() {
		return dao.countAll();
	}

	public int countByPoroperty(String property, Object value) {
		return dao.countByProperty(property, value);
	}

	public int countByProperties(Map<String, Object> properties) {
		return dao.countByProperties(properties);
	}

	@Override
	public int countAllNotDel() {
		return dao.countByProperty("deleteFlag", "N");
	}

	/**
	 * 按属性查找唯一对象.
	 */
	public T findUniqueByProperty(String property, Object value) {
		return dao.findUniqueByProperty(property, value);
	}
}
