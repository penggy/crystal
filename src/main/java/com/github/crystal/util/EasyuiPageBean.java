package com.github.crystal.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.github.crystal.dao.HDao;
import com.github.crystal.dao.PageBean;

public class EasyuiPageBean<T> {

	private long total;
	private List<T> rows = new ArrayList<T>();
	
	public static <T> EasyuiPageBean<T> pageQuery(HttpServletRequest request,HDao dao,String hql,Object... values){
		int page = NumberUtils.toInt(request.getParameter("page"),1);
		int rows = NumberUtils.toInt(request.getParameter("rows"),10);
		PageBean<T> pageBean = new PageBean<T>((page - 1) * rows, rows);
		String sort = request.getParameter("sort");
		if(StringUtils.isNotEmpty(sort)){
			String pOrderStr = "\\s*(o|O)(r|R)(d|D)(e|E)(r|R)\\s+(b|B)(y|Y).*$";
			hql = hql.replaceFirst(pOrderStr, "");
			hql += " order by " + sort + " " + request.getParameter("order");
		}
		dao.find(pageBean, hql, values);
		return new EasyuiPageBean<T>(pageBean);
	}
	
	public EasyuiPageBean(){
	}
	
	public EasyuiPageBean(PageBean<T> pageBean){
		this.total = pageBean.getCount();
		this.rows = pageBean.getData();
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<T> getRows() {
		return rows;
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
	}

}
