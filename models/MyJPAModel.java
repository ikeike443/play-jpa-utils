/**
 * 
 */
package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import play.db.jpa.JPA;
import play.db.jpa.JPASupport;

/**
 * <pre>
 * support for recording create/update datetime and making disable function instead of deleting
 * 
 * </pre>
 * @see {@link play.db.jpa.JPASupport}
 * @author ikeda.t
 *
 */
@MappedSuperclass
public abstract class MyJPAModel extends JPASupport {

	public Date createDateTime;
	public Date updateDateTime;
	public int status = 0;
	
	/**
	 * <pre>
	 * saveのオーバーライド
	 * createDateTimeとupdateDateTimeの自動更新をサポート
	 * @see {@link play.db.jpa.JPASupport#save()}
	 * </pre>
	 * @param <T>
	 * @return オブジェクト
	 */
	@Override
	public <T extends JPASupport> T save(){
		Date nowDateTime =  new DateTime().toDate();
		this.createDateTime = (this.createDateTime==null)? nowDateTime : this.createDateTime;
		this.updateDateTime = nowDateTime;	
	
		return super.save();
	}
	
	/**
	 * <pre>
	 * 論理削除のサポート
	 * statusを1に更新する
	 * </pre>
	 * @param <T>
	 * @return 
	 */
	public <T extends JPASupport> T disable(){
		this.status = 1;
		return this.save();
	}
	
	@Override
	public String toString(){
		return new ReflectionToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
	
    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
