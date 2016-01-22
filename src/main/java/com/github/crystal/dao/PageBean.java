package com.github.crystal.dao;

import java.util.ArrayList;
import java.util.List;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * 分页实体
 * 
 * @author <a href=mailto:wu.peng@starit.com.cn>wu.peng</a> 2012-6-12
 *
 * @param <T>
 */
@ApiModel("分页数据")
public class PageBean<T> {

	@ApiModelProperty("当前页从总量第N条开始(从0算起)")
	private int start;
	@ApiModelProperty("每页大小")
	private int limit;
	@ApiModelProperty("实际总量")
	private long count;
	@ApiModelProperty("分页数据")
	private List<T> data = new ArrayList<T>();

	public PageBean() {

	}

	public PageBean(int start, int limit) {
		this.start = start;
		this.limit = limit;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public String toString() {
		return "PageBean [start=" + start + ", limit=" + limit + ", count=" + count + ", data=" + data + "]";
	}

}
