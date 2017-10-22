package de.uni_leipzig.search_engine_uni.aspects;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
@Order(2)
public class EnrichResponsesWithViewAspect
{
	private static final List<MediaType> JSON_TYPES = Collections.unmodifiableList(Arrays.asList(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON));
	
	private static final MappingJackson2JsonView MAPPING_TO_JACKSON_VIEW = new MappingJackson2JsonView();
	
	private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private static Map<String, ?> proceedJointPointAndParseReturnValueAsModel(ProceedingJoinPoint proceedingJointPoint) throws Throwable
	{
		return OBJECT_MAPPER.convertValue(proceedingJointPoint.proceed(), new TypeReference<Map<String, Object>>() {});
	}
	
	private static final HttpHeaders extractHttpHeaderArgument(JoinPoint thisJointPoint)
	{
		return Arrays.asList(thisJointPoint.getArgs()).stream()
			.filter(o -> o instanceof HttpHeaders).
			map(o -> (HttpHeaders)o)
			.findFirst().orElse(null);
	}
	
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))"
		+ " && ! execution(* de.uni_leipzig.search_engine_uni.controller.RedirectController.redirect(..))")
	public Object renderResponseRegardingToRequestedContentType(ProceedingJoinPoint thisJointPoint) throws Throwable
	{
		HttpHeaders headers = extractHttpHeaderArgument(thisJointPoint);
		
		if(headers == null)
		{
			throw new RuntimeException();
		}
		
		Map<String, ?> returnModel = proceedJointPointAndParseReturnValueAsModel(thisJointPoint);
		
		if(headers != null && JSON_TYPES.contains(headers.getContentType()))
		{
			return new ModelAndView(MAPPING_TO_JACKSON_VIEW, returnModel);
		}
		
		return new ModelAndView(thisJointPoint.getSignature().getName(), returnModel);
	}
}
