package de.uni_leipzig.search_engine_uni.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Aspect
@Component
@Order(1)
public class RequestPerformanceLoggingAspect
{
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))")
	public Object measureExecutionTime(ProceedingJoinPoint thisJointPoint) throws Throwable
	{
		long start = System.currentTimeMillis();
		Object ret = thisJointPoint.proceed();
		
		Long durationInMilliseconds = (System.currentTimeMillis() - start);
		System.out.println("Call to "+ thisJointPoint.getSignature().getName() +" took "+ durationInMilliseconds);
		
		if(ret instanceof ModelAndView)
		{
			((ModelAndView)ret).getModel().put("durationInMilliseconds", durationInMilliseconds);
		}
		
		return ret;
	}
}
