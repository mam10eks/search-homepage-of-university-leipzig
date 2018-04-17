package de.uni_leipzig.search_engine.backend.aspects;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the content negotiation with a client.
 * <p>
 * There are are those cases:
 * <p>
 * <ul>
 * 		<li>A client {@link EnrichResponsesWithViewAspect#JSON_TYPES wants to retrive json}:
 * 			In that case the model is returned as json.</li>
 * 		<li>Else: The client gets the response parsed into html: In that case the model is rendered into an
 * 			thymeleaf template which is specified by the method name which creates the response model.</li>
 * 		<li>Redirects are not touched at all (since those methods doesnt deliver content in their response).</li>
 * 		<li>All methods anotated with {@link ResponseBody} are not touched at all (since those methods deliver their responses in their own defined way)</li>
 * </ul>
 * @author Maik Fröbe
 *
 */
@Aspect
@Component
@Order(2)
public class EnrichResponsesWithViewAspect
{
	private static final List<String> JSON_TYPES = Collections.unmodifiableList(jsonContentTypeNames());
	
	private static final MappingJackson2JsonView MAPPING_TO_JACKSON_VIEW = new MappingJackson2JsonView();
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private static Map<String, ?> proceedJointPointAndParseReturnValueAsModel(ProceedingJoinPoint proceedingJointPoint) throws Throwable
	{
		return convertToModel(proceedingJointPoint.proceed());
	}
	
	private static List<String> jsonContentTypeNames()
	{
		return Arrays.asList(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON).stream()
			.map(Object::toString)
			.collect(Collectors.toList());
	}
	
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))"
		+ " && !@annotation(org.springframework.web.bind.annotation.ResponseBody)"
		+ " && ! execution(* de.uni_leipzig.search_engine.backend.controller.redirect.RedirectController.redirect(..))")
	public Object renderResponseRegardingToRequestedContentType(ProceedingJoinPoint thisJointPoint) throws Throwable
	{
		String contentType = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
			.getHeader(HttpHeaders.CONTENT_TYPE);
		
		Map<String, ?> returnModel = proceedJointPointAndParseReturnValueAsModel(thisJointPoint);
		
		if(contentType != null && JSON_TYPES.contains(contentType))
		{
			return new ModelAndView(MAPPING_TO_JACKSON_VIEW, returnModel);
		}
		
		return new ModelAndView(thisJointPoint.getSignature().getName(), returnModel);
	}
	
	public static Map<String, Object> convertToModel(Object o)
	{
		return OBJECT_MAPPER.convertValue(o, new TypeReference<Map<String, Object>>() {});
	}
}
