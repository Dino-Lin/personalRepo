package com.dino.hibernate.page;

import java.util.List;

@SuppressWarnings({"serial","rawtypes"})
public class Pagination extends SimplePage implements java.io.Serializable,
		Paginable {

	public Pagination() {
	}

	public Pagination(int pageNo, int pageSize, int totalCount) {
		super(pageNo, pageSize, totalCount);
	}

	public Pagination(int pageNo, int pageSize, int totalCount, List list) {
		super(pageNo, pageSize, totalCount);
		this.list = list;
	}

	public int getFirstResult() {
	    int ret =(pageNo - 1) * pageSize < 0 ? 0 : (pageNo - 1) * pageSize; 
		return ret;
	}

	/**
	 * 当前页的数据
	 */
	private List list;

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}
}
