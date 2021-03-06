package com.dino.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;



/**
 * HQL语句分页查询
 * 
 * @author liufang
 * 
 */
public class Finder {
	protected Finder() {
	}

	public Finder(String hql) {
		hqlBuilder = new StringBuilder(hql);
	}

	public static Finder create(String hql) {
		Finder finder = new Finder(hql);
		return finder;
	}

	public Finder append(String hql) {
		hqlBuilder.append(hql);
		return this;
	}

	/**
	 * 获得原始hql语句
	 * 
	 * @return
	 */
	public String getOrigHql() {
		return hqlBuilder.toString();
	}

	/**
	 * 获得查询数据库记录数的hql语句。（复杂语句存在问题，可能出现条数不对的问题，建议复杂语句单独处理）
	 * 
	 * @return
	 */
	public String getRowCountHql() {
		String hql = hqlBuilder.toString();

		int fromIndex = hql.toLowerCase().lastIndexOf(FROM);

		hql = hql.substring(fromIndex);
		String rowCountHql = hql.replace(HQL_FETCH, "");

		int index = rowCountHql.indexOf(ORDER);
		if (index > 0) {
			rowCountHql = rowCountHql.substring(0, index);
		}
		return ROW_COUNT + rowCountHql;
	}

	/**
	 * 获得查询数据库记录数的hql语句(exist)。
	 * 
	 * @return
	 */
	public String getRowCountHqlForExist() {
		String hql = hqlBuilder.toString();

		int fromIndex = hql.toLowerCase().indexOf(FROM);

		hql = hql.substring(fromIndex);
		String rowCountHql = hql.replace(HQL_FETCH, "");

		int index = rowCountHql.indexOf(ORDER);
		if (index > 0) {
			rowCountHql = rowCountHql.substring(0, index);
		}
		return ROW_COUNT + rowCountHql;
	}



	/**
	 * 获得查询数据库记录数的hql语句(自定义from是语句中的第几个)。
	 * 
	 * @return
	 */
	public String getRowCountHqlForCustomFromIndex(int fromNum) {
		String hql = hqlBuilder.toString();
		while (fromNum > 1) {
			hql = hql.substring(hql.toLowerCase().indexOf(FROM));
			hql = hql.substring(4);
			fromNum--;
		}
		int fromIndex = hql.toLowerCase().indexOf(FROM);
		hql = hql.substring(fromIndex);
		String rowCountHql = hql.replace(HQL_FETCH, "");

		int index = rowCountHql.indexOf(ORDER);
		if (index > 0) {
			rowCountHql = rowCountHql.substring(0, index);
		}
		return ROW_COUNT + rowCountHql;
	}

	public int getFirstResult() {
		if (pageNo != null && pageSize != null) {
			return (pageNo - 1) * pageSize < 0 ? 0 : (pageNo - 1) * pageSize;
		}
		return firstResult;
	}

	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	public int getMaxResults() {
		if (pageSize != null)
			return pageSize;
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * 设置参数。与hibernate的Query接口一致。
	 * 
	 * @param param
	 * @param value
	 * @return
	 */
	public Finder setParam(String param, Object value) {
		return setParam(param, value, null);
	}

	/**
	 * 设置参数。与hibernate的Query接口一致。
	 * 
	 * @param param
	 * @param value
	 * @param type
	 * @return
	 */
	public Finder setParam(String param, Object value, Type type) {
		getParams().add(param);
		getValues().add(value);
		getTypes().add(type);
		return this;
	}

	/**
	 * 设置参数。与hibernate的Query接口一致。
	 * 
	 * @param paramMap
	 * @return
	 */
	public Finder setParams(Map<String, Object> paramMap) {
		for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
			setParam(entry.getKey(), entry.getValue());
		}
		return this;
	}

	/**
	 * 设置参数。与hibernate的Query接口一致。
	 * 
	 * @param name
	 * @param vals
	 * @param type
	 * @return
	 */
	public Finder setParamList(String name, Collection<Object> vals, Type type) {
		getParamsList().add(name);
		getValuesList().add(vals);
		getTypesList().add(type);
		return this;
	}

	/**
	 * 设置参数。与hibernate的Query接口一致。
	 * 
	 * @param name
	 * @param vals
	 * @return
	 */
	public Finder setParamList(String name, Collection<Object> vals) {
		return setParamList(name, vals, null);
	}

	/**
	 * 设置参数。与hibernate的Query接口一致。
	 * 
	 * @param name
	 * @param vals
	 * @param type
	 * @return
	 */
	public Finder setParamList(String name, Object[] vals, Type type) {
		getParamsArray().add(name);
		getValuesArray().add(vals);
		getTypesArray().add(type);
		return this;
	}

	/**
	 * 设置参数。与hibernate的Query接口一致。
	 * 
	 * @param name
	 * @param vals
	 * @return
	 */
	public Finder setParamList(String name, Object[] vals) {
		return setParamList(name, vals, null);
	}

	/**
	 * 将finder中的参数设置到query中。
	 * 
	 * @param query
	 */
	public Query setParamsToQuery(Query query) {
		if (params != null) {
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i) == null) {
					query.setParameter(params.get(i), values.get(i));
				} else {
					query.setParameter(params.get(i), values.get(i),
							types.get(i));
				}
			}
		}
		if (paramsList != null) {
			for (int i = 0; i < paramsList.size(); i++) {
				if (typesList.get(i) == null) {
					query.setParameterList(paramsList.get(i), valuesList.get(i));
				} else {
					query.setParameterList(paramsList.get(i),
							valuesList.get(i), typesList.get(i));
				}
			}
		}
		if (paramsArray != null) {
			for (int i = 0; i < paramsArray.size(); i++) {
				if (typesArray.get(i) == null) {
					query.setParameterList(paramsArray.get(i),
							valuesArray.get(i));
				} else {
					query.setParameterList(paramsArray.get(i),
							valuesArray.get(i), typesArray.get(i));
				}
			}
		}
		return query;
	}

	public void setSortFileds(Map<String, String> sortFields,
			String ObjectAnotherName) {
		if (sortFields != null&&!sortFields.keySet().isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder(" "+ORDER_BY);
			for (String key : sortFields.keySet()) {
				stringBuilder.append(" ");
				stringBuilder.append(ObjectAnotherName);
				stringBuilder.append(".");
				stringBuilder.append(key);
				stringBuilder.append(" ");
				stringBuilder.append(sortFields.get(key));
				stringBuilder.append(",");
			}
			String sortStatement = stringBuilder.toString();
			sortStatement = sortStatement.substring(0,
					sortStatement.lastIndexOf(","));
			hqlBuilder.append(sortStatement);
		}
	}

	public Query createQuery(Session s) {
		return setParamsToQuery(s.createQuery(getOrigHql()));
	}

	private List<String> getParams() {
		if (params == null) {
			params = new ArrayList<String>();
		}
		return params;
	}

	private List<Object> getValues() {
		if (values == null) {
			values = new ArrayList<Object>();
		}
		return values;
	}

	private List<Type> getTypes() {
		if (types == null) {
			types = new ArrayList<Type>();
		}
		return types;
	}

	private List<String> getParamsList() {
		if (paramsList == null) {
			paramsList = new ArrayList<String>();
		}
		return paramsList;
	}

	private List<Collection<Object>> getValuesList() {
		if (valuesList == null) {
			valuesList = new ArrayList<Collection<Object>>();
		}
		return valuesList;
	}

	private List<Type> getTypesList() {
		if (typesList == null) {
			typesList = new ArrayList<Type>();
		}
		return typesList;
	}

	private List<String> getParamsArray() {
		if (paramsArray == null) {
			paramsArray = new ArrayList<String>();
		}
		return paramsArray;
	}

	private List<Object[]> getValuesArray() {
		if (valuesArray == null) {
			valuesArray = new ArrayList<Object[]>();
		}
		return valuesArray;
	}

	private List<Type> getTypesArray() {
		if (typesArray == null) {
			typesArray = new ArrayList<Type>();
		}
		return typesArray;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getFromIndex() {
		return fromIndex;
	}

	public void setFromIndex(Integer fromIndex) {
		this.fromIndex = fromIndex;
	}

	private StringBuilder hqlBuilder;

	private List<String> params;
	private List<Object> values;
	private List<Type> types;

	private List<String> paramsList;
	private List<Collection<Object>> valuesList;
	private List<Type> typesList;

	private List<String> paramsArray;
	private List<Object[]> valuesArray;
	private List<Type> typesArray;

	private int firstResult = 0;

	private int maxResults = 0;

	private Integer pageNo;
	private Integer pageSize;

	private Integer fromIndex = 0;

	public static final String ROW_COUNT = "select count(*) ";
	public static final String FROM = "from";
	public static final String DISTINCT = "distinct";
	public static final String HQL_FETCH = "fetch";
	public static final String ORDER_BY = "order by";
	public static final String ORDER = "order ";


}