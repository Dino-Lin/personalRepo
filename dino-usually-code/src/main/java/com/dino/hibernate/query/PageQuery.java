package com.dino.hibernate.query;

/***
 * 分页查询基础对象
 * @author tanw
 *
 */
public abstract class PageQuery extends BaseQuery {
	protected Integer pageSize=20;
	protected Integer pageNo=1;

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	@Override
	public String toString() {
		return "PaginationQuery [pageSize=" + pageSize + ", pageNo=" + pageNo + "]";
	}

}
