package de.uni_leipzig.search_engine.backend.aspects;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import de.uni_leipzig.search_engine.events.SearchResultSelectedEvent;
import de.uni_leipzig.search_engine.events.WebResponseEvent;

/**
 * An aspect which logs all requests which are pointing to any endpoint which is delivered by this software.
 * Ensures also that a client is always identifyable by leveraging session cookies.
 * 
 * @author Maik Fröbe
 *
 */
@Aspect
@Component
@Order(1)
public class LoggingAspect
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);
	
	@Autowired
	private KafkaTopicProducer<WebResponseEvent> kafkaLoggingProducer;
	
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))")
	public Object logExecutionTimeOfRequests(ProceedingJoinPoint thisJointPoint) throws Throwable
	{
		long start = System.currentTimeMillis();
		Object ret = thisJointPoint.proceed();
		
		WebResponseEvent loggingEvent = new WebResponseEvent(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()));
		
		Long durationInMilliseconds = (System.currentTimeMillis() - start);
		
		LOGGER.info("Call to '{}' with arguments {} took {} ms for user with client identification '{}'", 
			thisJointPoint.getSignature().toString(), thisJointPoint.getArgs(),
			durationInMilliseconds, RequestContextHolder.currentRequestAttributes().getSessionId());

		if(ret instanceof ModelAndView)
		{
			((ModelAndView)ret).getModel().put("durationInMilliseconds", durationInMilliseconds);
			loggingEvent.setResponseModel(((ModelAndView)ret).getModel());
		}
		else if(ret instanceof RedirectView)
		{
			Map<String, Object> responseModel = new HashMap<>();
			responseModel.put(SearchResultSelectedEvent.LOCATION, ((RedirectView) ret).getUrl());
			loggingEvent.setResponseModel(responseModel);
		}
		
		kafkaLoggingProducer.logEvent(loggingEvent);

		return ret;
	}
	
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)"
			+ "&& execution(* de.uni_leipzig.search_engine.backend.controller.redirect.RedirectController..*(..))")
	public Object logRedirects(ProceedingJoinPoint thisJointPoint) throws Throwable
	{
		Object ret = thisJointPoint.proceed();
		
		if(!(ret instanceof RedirectView))
		{
			throw new RuntimeException("This aspect is expected to be woven around redirect methods which return instances of RedirectView.");
		}
		
		LOGGER.info("A user identified by '{}' is redirected due to a call to '{}' with arguments {} to the url '{}'. The user comes from '{}'.",
			RequestContextHolder.currentRequestAttributes().getSessionId(),
			thisJointPoint.getSignature().toString(), thisJointPoint.getArgs(),
			((RedirectView) ret).getUrl(), 
			 ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getHeader("referer"));
		
		return ret;
	}
}
