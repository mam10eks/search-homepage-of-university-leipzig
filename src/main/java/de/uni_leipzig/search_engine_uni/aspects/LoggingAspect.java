package de.uni_leipzig.search_engine_uni.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Aspect
@Component
@Order(1)
public class LoggingAspect
{
	Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);
	
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))")
	public Object logExecutionTimeOfRequests(ProceedingJoinPoint thisJointPoint) throws Throwable
	{
		long start = System.currentTimeMillis();
		Object ret = thisJointPoint.proceed();
		
		Long durationInMilliseconds = (System.currentTimeMillis() - start);
		
		LOGGER.info("Call to '{}' with arguments {} took {} ms for user with client identification '{}'", 
			thisJointPoint.getSignature().toString(), thisJointPoint.getArgs(),
			durationInMilliseconds, RequestContextHolder.currentRequestAttributes().getSessionId());

		if(ret instanceof ModelAndView)
		{
			((ModelAndView)ret).getModel().put("durationInMilliseconds", durationInMilliseconds);
		}
		
		return ret;
	}
	
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)"
			+ "&& execution(* de.uni_leipzig.search_engine_uni.controller.RedirectController..*(..))")
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
