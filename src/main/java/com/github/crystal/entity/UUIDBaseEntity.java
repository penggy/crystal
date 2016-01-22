package com.github.crystal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**
 * 实体bean基类
 * 
 * @author <a href=mailto:pengwu2@iflytek.com>wu.peng</a> 2012-6-18
 * 
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class UUIDBaseEntity implements Comparable<Object>,Serializable {
	
	private String id;

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid",strategy = "uuid")
	@Column(name = "id", length = 32)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UUIDBaseEntity other = (UUIDBaseEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public int compareTo(Object o) {
		if (o instanceof UUIDBaseEntity) {
			UUIDBaseEntity be = (UUIDBaseEntity) o;
			if (this.id != null && be.getId() != null) {
				return this.id.compareTo(be.getId());
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		return "UUIDBaseEntity [id=" + id + "]";
	}
	
}
