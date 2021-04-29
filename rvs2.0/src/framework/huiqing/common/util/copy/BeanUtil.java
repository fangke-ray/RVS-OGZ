package framework.huiqing.common.util.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.XssShieldUtil;
import framework.huiqing.common.util.validator.DoubleScaleValidator;
import framework.huiqing.common.util.validator.DoubleTypeValidator;
import framework.huiqing.common.util.validator.FullStringTypeValidator;
import framework.huiqing.common.util.validator.HalfStringTypeValidator;
import framework.huiqing.common.util.validator.IntegerTypeValidator;
import framework.huiqing.common.util.validator.LongTypeValidator;
import framework.huiqing.common.util.validator.MaxlengthValidator;
import framework.huiqing.common.util.validator.RequiredValidator;
import framework.huiqing.common.util.validator.UnsignedValidator;
import framework.huiqing.common.util.validator.Validators;

public class BeanUtil {

	private static volatile boolean initialized = false;

	private static ConcurrentHashMap<String, ConcurrentHashMap<String, BeanFieldEntity>> beanDescCache =
			new ConcurrentHashMap<String, ConcurrentHashMap<String, BeanFieldEntity>>(200);

	static {
		initialize();
	}

	private static void initialize() {
		beanDescCache.clear();
		initialized = true;
	}

	public static Map<String, BeanFieldEntity> getBeanDesc(Class<?> beanClass) throws NullPointerException {
		if (beanClass == null) {
			throw new NullPointerException("The beanClass parameter is null.");
		}
		if (!initialized) {
			initialize();
		}
		String className = beanClass.getName();
		if (!beanDescCache.containsKey(className)) {
			beanDescCache.put(className, createBeanDesc(beanClass));
		}
		return beanDescCache.get(className);
	}

	private static ConcurrentHashMap<String, BeanFieldEntity> createBeanDesc(Class<?> beanClass) {

		ConcurrentHashMap<String, BeanFieldEntity> beanDesc = new ConcurrentHashMap<String, BeanFieldEntity>();
		return createBeanDesc(beanClass, beanDesc);
	}
	private static ConcurrentHashMap<String, BeanFieldEntity> createBeanDesc(Class<?> beanClass, ConcurrentHashMap<String, BeanFieldEntity> beanDesc) {

		Field[] fa = beanClass.getDeclaredFields();
		for (Field f : fa) {
			if (Modifier.isStatic(f.getModifiers())) 
				continue;

			String fieldName = f.getName();
			BeanFieldEntity fieldDesc = new BeanFieldEntity();
			BeanField attr = f.getAnnotation(BeanField.class);
			if (attr == null) {
				fieldDesc.setName(fieldName);
				fieldDesc.setTitle(fieldName);
			} else {
				fieldDesc.setCipher(attr.cipher());
				fieldDesc.setLength(attr.length());
				fieldDesc.setName(attr.name());
				fieldDesc.setNotNull(attr.notNull());
				fieldDesc.setPrimaryKey(attr.primaryKey());
				fieldDesc.setScale(attr.scale());
				fieldDesc.setTitle(attr.title());
				fieldDesc.setType(attr.type());
			}
			beanDesc.put(fieldName, fieldDesc);
		}
		if(beanClass.getSuperclass()!=null){
			Class<?> superclass = beanClass.getSuperclass();
			if (!"org.apache.struts.action.ActionForm".equals(superclass.getName())) {
				beanDesc = createBeanDesc(superclass, beanDesc);
			};
		}

		return beanDesc;
	}

	public static void copyToBean(Object src, Object dest, CopyOptions cos) {
		if (src == null) {
			throw new NullPointerException("The src parameter is null.");
		}
		if (dest == null) {
			throw new NullPointerException("The dest parameter is null.");
		}

		Map<String, BeanFieldEntity> srcBeanDesc = getBeanDesc(src.getClass());

		for (String srcPropertyName : srcBeanDesc.keySet()) {
			BeanFieldEntity srcPropertyDesc = srcBeanDesc.get(srcPropertyName);

			if (srcPropertyDesc == null) {
				continue;
			}

			if (cos != null && !cos.isTargetProperty(srcPropertyName)) {
				continue;
			}

			String formValue = "";
			try {
				Method getMethod = src.getClass().getMethod("get" + CommonStringUtil.capitalize(srcPropertyName));
				Object formObj = getMethod.invoke(src);
				if (!(formObj instanceof String)) continue;
				formValue = (String) formObj;
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NullPointerException("get" +  CommonStringUtil.capitalize(srcPropertyName) + "() of " + src.getClass().getName() + " is Error!");
			}

			if (cos != null && !cos.isTargetValue(formValue)) {
				continue;
			}

			Converter<?> c = null;
			if (cos != null) {
				c = cos.converterMap.get(srcPropertyName);
			}
			if (c == null) {
				c = getConverter(srcPropertyDesc.getType(), srcPropertyDesc.getScale());
			}

			Object beanValue;
			if (c != null) {
				beanValue = c.getAsObject(formValue);
			} else {
				beanValue = formValue;
				if (srcPropertyDesc.isCipher() && !CommonStringUtil.isEmpty(formValue)) {
					beanValue = CryptTool.encrypttoStr(formValue);
				}
			}
			Class<?> destType = getDestType(srcPropertyDesc.getType());

			String destPropertyName = null;
			if (cos != null && cos.renameMap.containsKey(srcPropertyName)) {
				destPropertyName = cos.renameMap.get(srcPropertyName);
			} else {
				destPropertyName = srcPropertyDesc.getName();
			}
			if (destPropertyName == null) {
				destPropertyName = srcPropertyName;
			}

			try {
				Method setMethod = dest.getClass().getMethod("set" + CommonStringUtil.capitalize(destPropertyName), destType);
				setMethod.invoke(dest, destType.cast(beanValue));
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NullPointerException("set" + CommonStringUtil.capitalize(destPropertyName) + "() of " + dest.getClass().getName() + " is Error!");
			}
		}
	}

	public static void copyToForm(Object src, Object dest, CopyOptions cos) {
		if (src == null) {
			throw new NullPointerException("The src parameter is null.");
		}
		if (dest == null) {
			throw new NullPointerException("The dest parameter is null.");
		}
		
		Map<String, BeanFieldEntity> srcBeanDesc = getBeanDesc(src.getClass()); // TODO
		Map<String, BeanFieldEntity> destBeanDesc = getBeanDesc(dest.getClass());

		for (String srcPropertyName : srcBeanDesc.keySet()) {
			BeanFieldEntity srcPropertyDesc = srcBeanDesc.get(srcPropertyName);

			if (srcPropertyDesc == null) {
				continue;
			}

			if (cos != null && !cos.isTargetProperty(srcPropertyName)) {
				continue;
			}
			

			Object beanValue = null;
			try {
				Method getMethod = src.getClass().getMethod("get" + CommonStringUtil.capitalize(srcPropertyName));
				beanValue = getMethod.invoke(src);
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NullPointerException("get" +  CommonStringUtil.capitalize(srcPropertyName) + "() of " + src.getClass().getName() + " is Error!");
			}

			if (cos != null && !cos.isTargetValue(beanValue)) {
				continue;
			}

			String destPropertyName = null;
			if (cos != null && cos.renameMap.containsKey(srcPropertyName)) {
				destPropertyName = cos.renameMap.get(srcPropertyName);
			} else {
				destPropertyName = getFormTarget(srcPropertyName, destBeanDesc);
			}

			if (destPropertyName == null) {
				continue;
			}
			BeanFieldEntity destPropertyDesc = destBeanDesc.get(destPropertyName);
			if (destPropertyDesc == null) {
				continue;
			}
			Converter<?> c = null;
			if (cos != null) {
				c = cos.converterMap.get(srcPropertyName);
			}
			if (c == null) {
				c = getConverter(destPropertyDesc.getType(), destPropertyDesc.getScale());
			}

			String formValue;
			if (c != null) {
				formValue = c.getAsString(beanValue);
			} else {
				formValue = (beanValue == null) ? null : beanValue.toString();
				if (destPropertyDesc.isCipher() && !CommonStringUtil.isEmpty(formValue)) {
					formValue = CryptTool.decrypt(formValue);
				}
			}

			try {
				Method setMethod = dest.getClass().getMethod("set" + CommonStringUtil.capitalize(destPropertyName), String.class);
				setMethod.invoke(dest, formValue);
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NullPointerException("set" + CommonStringUtil.capitalize(destPropertyName) + "() of " + dest.getClass().getName() + " is Error!");
			}
		}
	}

	public static <T extends ActionForm> void copyToFormList(List<? extends Object> lcb, List<T> lcf, CopyOptions cos, Class<T> formclass) {
		if (lcb == null || lcf == null) {
			return;
		}
		//long s = new Date().getTime();
		for (Object bean : lcb) {

			T form = null;
			try {
				form = formclass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			BeanUtil.copyToForm(bean, form, cos);
			lcf.add(form);
		}
		//System.out.println("st:" + (new Date().getTime() - s));
	}

	public static byte CHECK_TYPE_ALL = 0;
	public static byte CHECK_TYPE_PASSEMPTY = 1;
	public static byte CHECK_TYPE_ONLYKEY = 2;

	public static Validators createBeanValidators(Object bean) {
		return createBeanValidators(bean, CHECK_TYPE_ALL);
	}
	public static Validators createBeanValidators(Object bean, byte checkType) {
		if (bean == null) {
			return null;
		}
		Map<String, BeanFieldEntity> srcBeanDesc = getBeanDesc(bean.getClass());

		Map<String, Object> parameters = new HashMap<String, Object>();
		Validators validators = new Validators(parameters);

		for(String field : srcBeanDesc.keySet()) {
			BeanFieldEntity beanFieldEntity = srcBeanDesc.get(field);
			String label = beanFieldEntity.getTitle();
			Object value = null;
			try {
				Method getMethod = bean.getClass().getMethod("get" + CommonStringUtil.capitalize(field));
				value = getMethod.invoke(bean);
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NullPointerException("get" +  CommonStringUtil.capitalize(field) + "() of " + bean.getClass().getName() + " is Error!");
			}
			parameters.put(field, value);
			if (checkType == CHECK_TYPE_ONLYKEY && !beanFieldEntity.isPrimaryKey()) {
				continue;
			}

			if ((checkType != CHECK_TYPE_PASSEMPTY && beanFieldEntity.isNotNull() && !beanFieldEntity.isPrimaryKey())
					|| checkType == CHECK_TYPE_ONLYKEY){
				validators.add(field, new RequiredValidator(label));
			}

			if (beanFieldEntity.getLength() > 0) {
				validators.add(field, new MaxlengthValidator(label, beanFieldEntity.getLength()));
			}
			FieldType fieldType = beanFieldEntity.getType();
			if (fieldType.equals(FieldType.Date)) {
			} else if (fieldType.equals(FieldType.DateTime)) {
			} else if (fieldType.equals(FieldType.TimeStamp)) {
			} else if (fieldType.equals(FieldType.Double)) {
				int length = beanFieldEntity.getLength();
				int scale = beanFieldEntity.getScale();
				validators.add(field, new DoubleTypeValidator(label));
				if (length > 0 && scale > 0) {
					validators.add(field, new DoubleScaleValidator(length, scale, label));
				}
			} else if (fieldType.equals(FieldType.FullString)) {
				validators.add(field, new FullStringTypeValidator(label));
			} else if (fieldType.equals(FieldType.HalfString)) {
				validators.add(field, new HalfStringTypeValidator(label));
			} else if (fieldType.equals(FieldType.Integer)) {
				validators.add(field, new IntegerTypeValidator(label));
			} else if (fieldType.equals(FieldType.Long)) {
				validators.add(field, new LongTypeValidator(label));
			} else if (fieldType.equals(FieldType.UDouble)) {
				int length = beanFieldEntity.getLength();
				int scale = beanFieldEntity.getScale();
				validators.add(field, new DoubleTypeValidator(label));
				if (length > 0 && scale > 0) {
					validators.add(field, new DoubleScaleValidator(length, scale, label));
				}
				validators.add(field, new UnsignedValidator(label));
			} else if (fieldType.equals(FieldType.UInteger)) {
				validators.add(field, new IntegerTypeValidator(label));
				validators.add(field, new UnsignedValidator(label));
			} else if (fieldType.equals(FieldType.ULong)) {
				validators.add(field, new LongTypeValidator(label));
				validators.add(field, new UnsignedValidator(label));
			} else if (fieldType.equals(FieldType.Word)) { //英数字
				
			}
		}

		return validators;
	}

	public static void checkPostXss(Object bean) {
		if (bean == null) {
			return;
		}
		Map<String, BeanFieldEntity> srcBeanDesc = getBeanDesc(bean.getClass());
		for(String field : srcBeanDesc.keySet()) {
			BeanFieldEntity beanFieldEntity = srcBeanDesc.get(field);

			FieldType fieldType = beanFieldEntity.getType();

			switch(fieldType) {
			case Bool :
			case Date :
			case DateTime :
			case Double :
			case Integer :
			case Key :
			case Long :
			case TimeStamp :
			case UDouble :
			case UInteger :
			case ULong :
				continue;
			default:
			}

			if (beanFieldEntity.getLength() <= 16 && beanFieldEntity.getLength() >= 0) 
				continue;

			if (beanFieldEntity.isCipher())
				continue;
		
			Object value = null;
			try {
				Method getMethod = bean.getClass().getMethod("get" + CommonStringUtil.capitalize(field));
				value = getMethod.invoke(bean);
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NullPointerException("get" +  CommonStringUtil.capitalize(field) + "() of " + bean.getClass().getName() + " is Error!");
			}

			if (value == null)
				continue;
			String sVal = value.toString();
			String dVal = XssShieldUtil.stripXss(sVal);

			if (!sVal.equals(dVal)) {
				try {
					Method setMethod = bean.getClass().getMethod("set" + CommonStringUtil.capitalize(field), String.class);
					value = setMethod.invoke(bean, dVal);
				} catch (Exception e) {
					throw new NullPointerException("set" +  CommonStringUtil.capitalize(field) + "() of " + bean.getClass().getName() + " is Error!");
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static String getFormTarget(String srcPropertyName, Class<?> beanClass) {
		Map<String, BeanFieldEntity> destBeanDesc = getBeanDesc(beanClass);
		return getFormTarget(srcPropertyName, destBeanDesc);
	}

	private static String getFormTarget(String srcPropertyName, Map<String, BeanFieldEntity> destBeanDesc) {
		if (destBeanDesc == null || destBeanDesc.size() == 0)
			return null;

		for (String destBeanFieldName : destBeanDesc.keySet()){
			BeanFieldEntity beanFieldEntity = destBeanDesc.get(destBeanFieldName);
			if (beanFieldEntity != null && srcPropertyName.equals(beanFieldEntity.getName())) {
				return destBeanFieldName;
			}
		}
		return null;
	}

	private static Converter<?> getConverter(FieldType type, int scalePattern) {
		switch (type) {
		case Bool:
			return BooleanConverter.getInstance(scalePattern);
		case Double:
		case UDouble:
			return BigDecimalConverter.getInstance();
		case Integer:
		case UInteger:
			return IntegerConverter.getInstance();
		case Long:
		case ULong:
			return LongConverter.getInstance();
		case Date:
			return DateConverter.getInstance(DateUtil.DATE_PATTERN);
		case DateTime:
			return DateConverter.getInstance(DateUtil.DATE_TIME_PATTERN);
		case TimeStamp:
			return DateConverter.getInstance(DateUtil.DATE_TIME_PATTERN); // TODO
		default:
			return null;
		}
	}

	private static Class<?> getDestType(FieldType type) {
		switch (type) {
		case Bool:
			return Boolean.class;
		case Double:
		case UDouble:
			return BigDecimal.class;
		case Integer:
		case UInteger:
			return Integer.class;
		case Long:
		case ULong:
			return Long.class;
		case Date:
		case DateTime:
		case TimeStamp:
			return Date.class;
		default:
			return String.class;
		}
	}

}
