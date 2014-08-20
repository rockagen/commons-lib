/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rockagen.commons.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON UTILS
 * 
 * @author RA
 * @since JDK1.6
 * @since JACKSON 2.2.0
 */
public class JsonUtil {

	// ~ Instance fields ==================================================

	/**
	 * 
	 */
	private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
	/**
	 * ObjectMapper
	 */
	private static final ThreadLocal<ObjectMapper> threadMapper = new ThreadLocal<ObjectMapper>();
	/**
	 * JsonFactory
	 */
	private static final ThreadLocal<JsonFactory> threadJsonFactory = new ThreadLocal<JsonFactory>();

	// ~ Constructors ==================================================

	/**
	 */
	private JsonUtil() {

	}

	// ~ Methods ==================================================

	/**
	 * return a ObjectMapper if exist,else new ObjectMapper
	 * 
	 * @return ObjectMapper
	 */
	public static ObjectMapper getMapper() {

		ObjectMapper mapper = threadMapper.get();
		if (mapper == null) {
			mapper = initMapper();
			threadMapper.set(mapper);

		}
		return mapper;
	}

	/**
	 * Initialize mapper
	 * 
	 * @return initialized {@link ObjectMapper}
	 */
	private static ObjectMapper initMapper() {

		ObjectMapper mapper = new ObjectMapper();
		// to enable standard indentation ("pretty-printing"):
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		// to allow serialization of "empty" POJOs (no properties to serialize)
		// (without this setting, an exception is thrown in those cases)
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// set writer flush after writer value
		mapper.enable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
		// ObjectMapper will call close() and root values that implement
		// java.io.Closeable;
		// including cases where exception is thrown and serialization does not
		// completely succeed.
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
		// to write java.util.Date, Calendar as number (timestamp):
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		// disable default date to timestamp
		mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);

		// DeserializationFeature for changing how JSON is read as POJOs:

		// to prevent exception when encountering unknown property:
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// disable default date to timestamp
		mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
		// to allow coercion of JSON empty String ("") to null Object value:
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		// Set Default date fromat
		mapper.setDateFormat(df);

		return mapper;

	}

	/**
	 * return a JsonFactory if exist,else new JsonFactory
	 * 
	 * @return JsonFactory
	 */
	public static JsonFactory getJsonFactory() {

		JsonFactory jsonFactory = threadJsonFactory.get();
		if (jsonFactory == null) {
			jsonFactory = new JsonFactory();
			// JsonParser.Feature for configuring parsing settings:
			// to allow C/C++ style comments in JSON (non-standard, disabled by
			// default)
			jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
			// to allow (non-standard) unquoted field names in JSON:
			jsonFactory.disable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
			// to allow use of apostrophes (single quotes), non standard
			jsonFactory.disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

			// JsonGenerator.Feature for configuring low-level JSON generation:

			// no escaping of non-ASCII characters:
			jsonFactory.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
			threadJsonFactory.set(jsonFactory);

		}
		return jsonFactory;
	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * List,Array,Map
	 * </p>
	 * <code><pre>
	 * clazz= new TypeReference&lt;List&lt;MyBean&gt;&gt;(){};
	 * 
	 * e.g:
	 * toBean(jsonStr,new TypeReference&lt;List&lt;MyBean&gt;&gt;(){})
	 * 
	 * toBean(jsonStr, MyBean[])
	 * 
	 * toBean(jsonStr, new TypeReference&lt;Map&lt;String, Map&lt;String, MyBean&gt;&gt;(){})
	 * </code></p>
	 * 
	 * @param jsonStr
	 * @param valueTypeRef
	 * @return bean
	 */
	public static <T> T toBean(String jsonStr, TypeReference<T> valueTypeRef) {
		if (valueTypeRef == null || jsonStr == null)
			return null;
		return toBean(new MyJsonParser(jsonStr), valueTypeRef);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * List,Array,Map
	 * </p>
	 * <code><pre>
	 * clazz= new TypeReference&lt;List&lt;MyBean&gt;&gt;(){};
	 * 
	 * e.g:
	 * toBean(jsonBytes,new TypeReference&lt;List&lt;MyBean&gt;&gt;(){})
	 * 
	 * toBean(jsonBytes, MyBean[])
	 * 
	 * toBean(jsonBytes, new TypeReference&lt;Map&lt;String, Map&lt;String, MyBean&gt;&gt;(){})
	 * </code></p>
	 * 
	 * @param jsonBytes
	 * @param valueTypeRef
	 * @return bean
	 */
	public static <T> T toBean(byte[] jsonBytes, TypeReference<T> valueTypeRef) {
		if (valueTypeRef == null || jsonBytes == null)
			return null;
		return toBean(new MyJsonParser(jsonBytes), valueTypeRef);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * List,Array,Map
	 * </p>
	 * <code><pre>
	 * clazz= new TypeReference&lt;List&lt;MyBean&gt;&gt;(){};
	 * 
	 * e.g:
	 * toBean(jsonReader,new TypeReference&lt;List&lt;MyBean&gt;&gt;(){})
	 * 
	 * toBean(jsonReader, MyBean[])
	 * 
	 * toBean(jsonReader, new TypeReference&lt;Map&lt;String, Map&lt;String, MyBean&gt;&gt;(){})
	 * </code></p>
	 * 
	 * @param jsonReader
	 * @param valueTypeRef
	 * @return bean
	 */
	public static <T> T toBean(Reader jsonReader, TypeReference<T> valueTypeRef) {
		if (valueTypeRef == null || jsonReader == null)
			return null;
		return toBean(new MyJsonParser(jsonReader), valueTypeRef);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * List,Array,Map
	 * </p>
	 * <code><pre>
	 * clazz= new TypeReference&lt;List&lt;MyBean&gt;&gt;(){};
	 * 
	 * e.g:
	 * toBean(jsonURL,new TypeReference&lt;List&lt;MyBean&gt;&gt;(){})
	 * 
	 * toBean(jsonURL, MyBean[])
	 * 
	 * toBean(jsonURL, new TypeReference&lt;Map&lt;String, Map&lt;String, MyBean&gt;&gt;(){})
	 * </code></p>
	 * 
	 * @param jsonURL
	 * @param valueTypeRef
	 * @return bean
	 */
	public static <T> T toBean(URL jsonURL, TypeReference<T> valueTypeRef) {
		if (valueTypeRef == null || jsonURL == null)
			return null;
		return toBean(new MyJsonParser(jsonURL), valueTypeRef);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * List,Array,Map
	 * </p>
	 * <code><pre>
	 * clazz= new TypeReference&lt;List&lt;MyBean&gt;&gt;(){};
	 * 
	 * e.g:
	 * toBean(jsonFile,new TypeReference&lt;List&lt;MyBean&gt;&gt;(){})
	 * 
	 * toBean(jsonFile, MyBean[])
	 * 
	 * toBean(jsonFile, new TypeReference&lt;Map&lt;String, Map&lt;String, MyBean&gt;&gt;(){})
	 * </code></p>
	 * 
	 * @param jsonFile
	 * @param valueTypeRef
	 * @return bean
	 */
	public static <T> T toBean(File jsonFile, TypeReference<T> valueTypeRef) {
		if (valueTypeRef == null || jsonFile == null)
			return null;
		return toBean(new MyJsonParser(jsonFile), valueTypeRef);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * call {@link MyJsonParser#getJsonParser()} return a JsonParser
	 * </p>
	 * 
	 * @param myJsonParser
	 * @param valueTypeRef
	 * @see #toBean(JsonParser, TypeReference)
	 * @return bean
	 */
	protected static <T> T toBean(MyJsonParser myJsonParser,
			TypeReference<T> valueTypeRef) {
		if (myJsonParser.getJsonParser() == null)
			return null;
		return toBean(myJsonParser.getJsonParser(), valueTypeRef);
	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * List,Array,Map
	 * </p>
	 * <code><pre>
	 * clazz= new TypeReference&lt;List&lt;MyBean&gt;&gt;(){};
	 * 
	 * e.g:
	 * toBean(jsonParser,new TypeReference&lt;List&lt;MyBean&gt;&gt;(){})
	 * 
	 * toBean(jsonParser, MyBean[])
	 * 
	 * toBean(jsonParser, new TypeReference&lt;Map&lt;String, Map&lt;String, MyBean&gt;&gt;(){})
	 * </code></p>
	 * 
	 * @param jsonParser
	 * @param valueTypeRef
	 * @return bean
	 */
	public static <T> T toBean(JsonParser jsonParser,
			TypeReference<T> valueTypeRef) {
		if (valueTypeRef == null || jsonParser == null)
			return null;

		T obj = null;
		try {
			obj = getMapper().readValue(jsonParser, valueTypeRef);
		} catch (JsonParseException e) {
			log.error("{}", e.getMessage(), e);
		} catch (JsonMappingException e) {
			log.error("{}", e.getMessage(), e);
		} catch (IOException e) {
			log.error("{}", e.getMessage(), e);
		} finally {
			try {
				jsonParser.close();
			} catch (IOException e) {
			}
		}
		return obj;
	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * e.g:
	 * </p>
	 * <code><pre>
	 * {"id" : idValue, "name" : nameValue, "aBean" : {"aBeanId" : aBeanIdValue, ...}} 
	 * </pre></code>
	 * <p>
	 * List,Array,Map... see {@link #toBean(String, TypeReference)}
	 * </p>
	 * 
	 * @param jsonStr
	 * @param clazz
	 * @return bean
	 */
	public static <T> T toBean(String jsonStr, Class<T> clazz) {
		if (clazz == null || jsonStr == null)
			return null;
		return toBean(new MyJsonParser(jsonStr), clazz);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * e.g:
	 * </p>
	 * <code><pre>
	 * {"id" : idValue, "name" : nameValue, "aBean" : {"aBeanId" : aBeanIdValue, ...}} 
	 * </pre></code>
	 * <p>
	 * List,Array,Map... see {@link #toBean(byte[], TypeReference)}
	 * </p>
	 * 
	 * @param jsonBytes
	 * @param clazz
	 * @return bean
	 */
	public static <T> T toBean(byte[] jsonBytes, Class<T> clazz) {
		if (clazz == null || jsonBytes == null)
			return null;
		return toBean(new MyJsonParser(jsonBytes), clazz);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * e.g:
	 * </p>
	 * <code><pre>
	 * {"id" : idValue, "name" : nameValue, "aBean" : {"aBeanId" : aBeanIdValue, ...}} 
	 * </pre></code>
	 * <p>
	 * List,Array,Map... see {@link #toBean(Reader, TypeReference)}
	 * </p>
	 * 
	 * @param jsonReader
	 * @param clazz
	 * @return bean
	 */
	public static <T> T toBean(Reader jsonReader, Class<T> clazz) {
		if (clazz == null || jsonReader == null)
			return null;
		return toBean(new MyJsonParser(jsonReader), clazz);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * e.g:
	 * </p>
	 * <code><pre>
	 * {"id" : idValue, "name" : nameValue, "aBean" : {"aBeanId" : aBeanIdValue, ...}} 
	 * </pre></code>
	 * <p>
	 * List,Array,Map... see {@link #toBean(URL, TypeReference)}
	 * </p>
	 * 
	 * @param jsonURL
	 * @param clazz
	 * @return bean
	 */
	public static <T> T toBean(URL jsonURL, Class<T> clazz) {
		if (clazz == null || jsonURL == null)
			return null;
		return toBean(new MyJsonParser(jsonURL), clazz);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * e.g:
	 * </p>
	 * <code><pre>
	 * {"id" : idValue, "name" : nameValue, "aBean" : {"aBeanId" : aBeanIdValue, ...}} 
	 * </pre></code>
	 * <p>
	 * List,Array,Map... see {@link #toBean(File, TypeReference)}
	 * </p>
	 * 
	 * @param jsonFile
	 * @param clazz
	 * @return bean
	 */
	public static <T> T toBean(File jsonFile, Class<T> clazz) {
		if (clazz == null || jsonFile == null)
			return null;
		return toBean(new MyJsonParser(jsonFile), clazz);

	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * call {@link MyJsonParser#getJsonParser()} return a JsonParser
	 * </p>
	 * 
	 * @param myJsonParser
	 * @param clazz
	 * @see #toBean(JsonParser, Class)
	 * @return bean
	 */
	protected static <T> T toBean(MyJsonParser myJsonParser, Class<T> clazz) {
		if (myJsonParser.getJsonParser() == null)
			return null;
		return toBean(myJsonParser.getJsonParser(), clazz);
	}

	/**
	 * Json string to java bean <br/>
	 * <p>
	 * e.g:
	 * </p>
	 * <code><pre>
	 * {"id" : idValue, "name" : nameValue, "aBean" : {"aBeanId" : aBeanIdValue, ...}} 
	 * </pre></code>
	 * <p>
	 * List,Array,Map... see {@link #toBean(String, TypeReference)}
	 * </p>
	 * 
	 * @param jsonParser
	 * @param clazz
	 * @return bean
	 */
	public static <T> T toBean(JsonParser jsonParser, Class<T> clazz) {
		if (clazz == null || jsonParser == null)
			return null;

		T obj = null;
		try {
			obj = getMapper().readValue(jsonParser, clazz);
		} catch (JsonParseException e) {
			log.error("{}", e.getMessage(), e);
		} catch (JsonMappingException e) {
			log.error("{}", e.getMessage(), e);
		} catch (IOException e) {
			log.error("{}", e.getMessage(), e);
		} finally {
			try {
				jsonParser.close();
			} catch (IOException e) {
			}
		}
		return obj;

	}

	/**
	 * Bean to json string
	 * 
	 * @param obj
	 *            bean object
	 * @return json string
	 */
	public static <T> String toJson(T obj) {
		StringWriter writer = new StringWriter();
		String jsonStr = "";
		JsonGenerator gen = null;
		try {
			gen = getJsonFactory().createGenerator(writer);
			getMapper().writeValue(gen, obj);
			writer.flush();
			jsonStr = writer.toString();

		} catch (IOException e) {
			log.error("{}", e.getMessage(), e);
		} finally {
			if (gen != null) {
				try {
					gen.close();
				} catch (IOException e) {
				}
			}

		}
		return jsonStr;
	}

	/**
	 * JsonParser Helper</br> provide to
	 * {@link JsonUtil#toBean(MyJsonParser, Class)} and
	 * {@link JsonUtil#toBean(MyJsonParser, TypeReference)}
	 */
	protected static class MyJsonParser {

		// ~ Instance fields ==================================================

		/**
		 * JsonParser
		 */
		private JsonParser jsonParser;

		// ~ Constructors ==================================================

		/**
		 * Set jsonParser from {@link JsonFactory#createParser(File)}
		 * 
		 * @param obj
		 */
		public MyJsonParser(File obj) {
			try {
				this.jsonParser = getJsonFactory().createParser(obj);
			} catch (JsonParseException e) {
				log.error("{}", e.getMessage(), e);
			} catch (IOException e) {
				log.error("{}", e.getMessage(), e);
			}
		}

		/**
		 * Set jsonParser from {@link JsonFactory#createParser(Reader)}
		 * 
		 * @param obj
		 */
		public MyJsonParser(Reader obj) {
			try {
				this.jsonParser = getJsonFactory().createParser(obj);
			} catch (JsonParseException e) {
				log.error("{}", e.getMessage(), e);
			} catch (IOException e) {
				log.error("{}", e.getMessage(), e);
			}
		}

		/**
		 * Set jsonParser from {@link JsonFactory#createParser(byte[])}
		 * 
		 * @param obj
		 */
		public MyJsonParser(byte[] obj) {
			try {
				this.jsonParser = getJsonFactory().createParser(obj);
			} catch (JsonParseException e) {
				log.error("{}", e.getMessage(), e);
			} catch (IOException e) {
				log.error("{}", e.getMessage(), e);
			}
		}

		/**
		 * Set jsonParser from {@link JsonFactory#createParser(String)}
		 * 
		 * @param obj
		 */
		public MyJsonParser(String obj) {
			try {
				this.jsonParser = getJsonFactory().createParser(obj);
			} catch (JsonParseException e) {
				log.error("{}", e.getMessage(), e);
			} catch (IOException e) {
				log.error("{}", e.getMessage(), e);
			}
		}

		/**
		 * Set jsonParser from {@link JsonFactory#createParser(URL)}
		 * 
		 * @param obj
		 */
		public MyJsonParser(URL obj) {
			try {
				this.jsonParser = getJsonFactory().createParser(obj);
			} catch (JsonParseException e) {
				log.error("{}", e.getMessage(), e);
			} catch (IOException e) {
				log.error("{}", e.getMessage(), e);
			}
		}

		/**
		 * Set jsonParser from {@link JsonFactory#createParser(InputStream)}
		 * 
		 * @param obj
		 */
		public MyJsonParser(InputStream obj) {
			try {
				this.jsonParser = getJsonFactory().createParser(obj);
			} catch (JsonParseException e) {
				log.error("{}", e.getMessage(), e);
			} catch (IOException e) {
				log.error("{}", e.getMessage(), e);
			}
		}

		// ~ Methods ==================================================

		/**
		 * Return a JsonParser by Constructors
		 * 
		 * @return jsonParser
		 */
		public JsonParser getJsonParser() {
			return jsonParser;
		}

	}

}
