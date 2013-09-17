/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.modcluster.load.metric;

import java.util.Arrays;
import java.util.LinkedHashSet;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.easymock.EasyMock;
import org.jboss.modcluster.load.metric.impl.MBeanAttributeRatioLoadMetric;
import org.jboss.modcluster.load.metric.impl.MBeanQueryLoadContext;
import org.jboss.modcluster.load.metric.impl.MBeanQueryLoadMetricSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paul Ferraro
 *
 */
@SuppressWarnings("boxing")
public class MBeanAttributeRatioLoadMetricTestCase
{
   private MBeanServer server = EasyMock.createStrictMock(MBeanServer.class); 
   private ObjectName name1;
   private ObjectName name2;
   private MBeanQueryLoadContext context;

   private String dividend;
   private String divisor;
   private LoadMetric<MBeanQueryLoadContext> metric;
   
   public MBeanAttributeRatioLoadMetricTestCase() throws MalformedObjectNameException
   {
      this("dividend", "divisor");
   }
   
   private MBeanAttributeRatioLoadMetricTestCase(String dividend, String divisor) throws MalformedObjectNameException
   {
      this(new MBeanAttributeRatioLoadMetric(new MBeanQueryLoadMetricSource("domain:*"), dividend, divisor), dividend, divisor);
   }
   
   protected MBeanAttributeRatioLoadMetricTestCase(LoadMetric<MBeanQueryLoadContext> metric, String dividend, String divisor)
   {
      this.metric = metric;
      this.dividend = dividend;
      this.divisor = divisor;
   }
   
   @Before
   public void prepare() throws MalformedObjectNameException
   {
      ObjectName pattern = ObjectName.getInstance("domain:*");
      this.name1 = ObjectName.getInstance("domain:name=test1");
      this.name2 = ObjectName.getInstance("domain:name=test2");
      
      EasyMock.expect(this.server.queryNames(pattern, null)).andReturn(new LinkedHashSet<ObjectName>(Arrays.asList(this.name1, this.name2)));
      
      EasyMock.replay(this.server);
      
      this.context = new MBeanQueryLoadContext(this.server, pattern);
      
      EasyMock.verify(this.server);
      EasyMock.reset(this.server);
   }
   
   @Test
   public void getLoad() throws Exception
   {
      EasyMock.expect(this.server.getAttribute(this.name1, this.dividend)).andReturn(1);
      EasyMock.expect(this.server.getAttribute(this.name2, this.dividend)).andReturn(2);
      
      EasyMock.expect(this.server.getAttribute(this.name1, this.divisor)).andReturn(2);      
      EasyMock.expect(this.server.getAttribute(this.name2, this.divisor)).andReturn(2);
      
      EasyMock.replay(this.server);
      
      double load = this.metric.getLoad(this.context);
      
      EasyMock.verify(this.server);
      
      Assert.assertEquals(0.75, load, 0.0);
      
      EasyMock.reset(this.server);
   }
}