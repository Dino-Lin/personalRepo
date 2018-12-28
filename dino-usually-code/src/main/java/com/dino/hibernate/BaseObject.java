package com.dino.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 
 * hibernate实体类基类，@MappedSuperclass,不会映射到数据库表，但继承它的子类实体在映射的时候会自动扫描该基类的映射属性
 * @author Dino
 *
 */
@MappedSuperclass
public class BaseObject implements Serializable {

	private static final long serialVersionUID = -3109648416000192157L;

	private Date createDt;
	private String creator;
	private Date updateDt;
	private String updator;
	private String deleteFlag;
	private String indexFlag;
	private int versionNo;

	@Column(name = "CREATE_DT", length = 50)
	public Date getCreateDt() {
		return createDt;
	}

	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}

	@Column(name = "CREATOR", length = 50)
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Column(name = "UPDATE_DT", length = 50)
	public Date getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(Date updateDt) {
		this.updateDt = updateDt;
	}

	@Column(name = "UPDATOR", length = 50)
	public String getUpdator() {
		return updator;
	}

	public void setUpdator(String updator) {
		this.updator = updator;
	}

	@Column(name = "DELETE_FLAG", length = 2)
	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	@Column(name = "INDEX_FLAG", length = 2)
	public String getIndexFlag() {
		return indexFlag;
	}

	public void setIndexFlag(String indexFlag) {
		this.indexFlag = indexFlag;
	}

	@Column(name = "VERSION_NO", length = 6)
	public int getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}

}
