package com.github.crystal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * 实体bean基类
 * 
 * @author <a href=mailto:pengwu2@iflytek.com>wu.peng</a> 2012-6-18
 * 
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class SequenceBaseEntity implements Comparable<Object>, Serializable {

	private Long id;

	@Id
	@GeneratedValue(generator = "g_entity")
	@GenericGenerator(name = "g_entity", strategy = "native", parameters = {
			@Parameter(name = "sequence", value = "seq_entity"),
			@Parameter(name = "parameters", value = "start with 1000") })
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "SequenceBaseEntity [id=" + id + "]";
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
		SequenceBaseEntity other = (SequenceBaseEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public int compareTo(Object o) {
		if (o instanceof SequenceBaseEntity) {
			SequenceBaseEntity be = (SequenceBaseEntity) o;
			if (this.id != null && be.getId() != null) {
				return this.id.compareTo(be.getId());
			}
		}
		return 0;
	}
}
