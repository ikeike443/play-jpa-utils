/**
 * 
 */
package validation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import javax.persistence.Id;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.configuration.annotation.Constraint;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.OValContext;
import play.db.jpa.JPA;
import play.db.jpa.JPASupport;


/**
 * <pre>
 * Unique constraint annotation
 * original from playframework ML
 * a little bit modified
 * </pre>
 * @see <a href="http://groups.google.com/group/play-framework/browse_thread/thread/e3eb06612f54153b/7fd1bbe92c2bf3a6?lnk=gst&q=unique#7fd1bbe92c2bf3a6"> 
 * http://groups.google.com/group/play-framework/browse_thread/thread/e3eb06612f54153b/7fd1bbe92c2bf3a6?lnk=gst&q=unique#7fd1bbe92c2bf3a6 </a>
 * 
 * TODO write unit test for db, mem and fs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.PARAMETER })
@Constraint(checkWith = Unique.UniqueCheck.class)
public @interface Unique {
	String message() default UniqueCheck.mes;

	String additionalHQL() default "";

	String[] additionalObjects() default {};

	public static class UniqueCheck extends AbstractAnnotationCheck<Unique> {
		public static final String mes = "validation.unique";
		private String additionalHQL;
		private String[] additionalObjects;

		@Override
		public void configure(Unique unique) {
			setMessage(unique.message());
			additionalHQL = unique.additionalHQL();
			additionalObjects = unique.additionalObjects();
		}

		public boolean isSatisfied(Object validatedObject, Object value,
				OValContext context, Validator validator) {
			if (value == null || value.toString().length() == 0) {
				return false;
			}
			JPASupport validatedModel = (JPASupport) validatedObject;
			String field = ((FieldContext) context).getField().getName();
			StringBuffer hql = new StringBuffer();
			hql.append("SELECT COUNT(*) FROM ");
			hql.append(validatedObject.getClass().getSimpleName());
			hql.append(" AS o WHERE o.");
			hql.append(field);
			hql.append("=?");
			Field idFld = getIdField(validatedModel);
			try {
				if (idFld.get(validatedModel) != null)
					hql.append(" AND id IS NOT ?");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (additionalHQL.length() > 0) {
				hql.append(" AND ");
				hql.append(additionalHQL);
			}
			Query query = JPA.em().createQuery(hql.toString());
			int index = 1;
			query.setParameter(index++, value);
			try {
				if (idFld.get(validatedModel) != null)
					query.setParameter(index++, idFld.get(validatedModel));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			for (String additionalObject : additionalObjects) {
				String methodName = "get"
						+ StringUtils.capitalize(additionalObject);
				try {
					Object val = validatedObject.getClass().getMethod(
							methodName).invoke(validatedObject);
					query.setParameter(index++, val);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			Long count = (Long) query.getSingleResult();
			return count.equals(0l);
		}
		
		public Field getIdField(Object obj){
			Field rtFld = null;
			for(Field field : obj.getClass().getFields()){
				if (field.isAnnotationPresent(Id.class)){
					rtFld = field;
					break;
				}
			}
			return rtFld;
			
		}
	}
}
