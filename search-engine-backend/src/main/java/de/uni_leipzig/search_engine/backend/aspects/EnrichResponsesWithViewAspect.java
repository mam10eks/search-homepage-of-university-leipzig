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
		return OBJECT_MAPPER.convertValue(proceedingJointPoint.proceed(), new TypeReference<Map<String, Object>>() {});
	}
	
	private static List<String> jsonContentTypeNames()
	{
		return Arrays.asList(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON).stream()
			.map(Object::toString)
			.collect(Collectors.toList());
	}
	
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))"
		+ " && ! execution(* de.uni_leipzig.search_engine.backend.controller.RedirectController.redirect(..))")
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
}
