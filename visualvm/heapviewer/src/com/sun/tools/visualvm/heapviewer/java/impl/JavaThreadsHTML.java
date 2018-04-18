/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.visualvm.heapviewer.java.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.JavaFrameGCRoot;
import org.netbeans.lib.profiler.heap.ThreadObjectGCRoot;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.heapwalk.details.api.DetailsSupport;
import com.sun.tools.visualvm.heapviewer.HeapContext;
import com.sun.tools.visualvm.heapviewer.java.ClassNode;
import com.sun.tools.visualvm.heapviewer.java.InstanceNode;
import com.sun.tools.visualvm.heapviewer.model.HeapViewerNode;
import com.sun.tools.visualvm.heapviewer.utils.HeapUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "JavaThreadsHTML_CannotResolveClassMsg=Cannot resolve class",
    "JavaThreadsHTML_CannotResolveInstanceMsg=Cannot resolve instance"
})
class JavaThreadsHTML {
    
    private static final String OPEN_THREADS_URL = "file:/stackframe/";     // NOI18N
//    private static final String THREAD_URL_PREFIX = "file://thread/";   // NOI18N
//    private static final String LINE_PREFIX = "&nbsp;&nbsp;&nbsp;&nbsp;"; // NOI18N
    
    
    static ThreadObjectGCRoot getOOMEThread(Heap heap) {
        Collection<GCRoot> roots = heap.getGCRoots();

        for (GCRoot root : roots) {
            if(root.getKind().equals(GCRoot.THREAD_OBJECT)) {
                ThreadObjectGCRoot threadRoot = (ThreadObjectGCRoot)root;
                StackTraceElement[] stackTrace = threadRoot.getStackTrace();
                
                if (stackTrace!=null && stackTrace.length>=1) {
                    StackTraceElement ste = stackTrace[0];
                    
                    if (OutOfMemoryError.class.getName().equals(ste.getClassName()) && "<init>".equals(ste.getMethodName())) {  // NOI18N
                        return threadRoot;
                    }
                }
            }
        }
        return null;
    }
    
    static HeapViewerNode getNode(URL url, HeapContext context) {
        String urls = url.toString();
                
        if (HeapUtils.isInstance(urls)) {
            final Instance instance = HeapUtils.instanceFromHtml(urls, context.getFragment().getHeap());
            if (instance != null) return new InstanceNode(instance);
            else ProfilerDialogs.displayError(Bundle.JavaThreadsHTML_CannotResolveInstanceMsg());
        } else if (HeapUtils.isClass(urls)) {
            JavaClass javaClass = HeapUtils.classFromHtml(urls, context.getFragment().getHeap());
            if (javaClass != null) return new ClassNode(javaClass);
            else ProfilerDialogs.displayError(Bundle.JavaThreadsHTML_CannotResolveClassMsg());
        }

        return null;
    }
    
    static String getThreads(HeapContext context) {
//        long start = System.currentTimeMillis();
        
//        boolean gotoSourceAvailable = context.getProject() != null && GoToSource.isAvailable();
        boolean gotoSourceAvailable = false;
        StringBuilder sb = new StringBuilder();
        Heap h = context.getFragment().getHeap();
        Collection<GCRoot> roots = h.getGCRoots();
        Map<ThreadObjectGCRoot,Map<Integer,List<JavaFrameGCRoot>>> javaFrameMap = computeJavaFrameMap(roots);
        ThreadObjectGCRoot oome = getOOMEThread(h);
        JavaClass javaClassClass = h.getJavaClassByName(Class.class.getName());
        // Use this to enable VisualVM color scheme for threads dumps:
        // sw.append("<pre style='color: #cc3300;'>"); // NOI18N
        sb.append("<pre>"); // NOI18N
        for (GCRoot root : roots) {
            if(root.getKind().equals(GCRoot.THREAD_OBJECT)) {
                ThreadObjectGCRoot threadRoot = (ThreadObjectGCRoot)root;
                Instance threadInstance = threadRoot.getInstance();
                if (threadInstance != null) {
                    String threadName = getThreadName(h, threadInstance);
                    Boolean daemon = (Boolean)threadInstance.getValueOfField("daemon"); // NOI18N
                    Integer priority = (Integer)threadInstance.getValueOfField("priority"); // NOI18N
                    Long threadId = (Long)threadInstance.getValueOfField("tid");    // NOI18N
                    Integer threadStatus = (Integer)threadInstance.getValueOfField("threadStatus"); // NOI18N
                    StackTraceElement stack[] = threadRoot.getStackTrace();
                    Map<Integer,List<JavaFrameGCRoot>> localsMap = javaFrameMap.get(threadRoot);
                    String style=""; // NOI18N

                    if (threadRoot.equals(oome)) {
                        style="style=\"color: #FF0000\""; // NOI18N
                    }                        
                    // --- Use this to enable VisualVM color scheme for threads dumps: ---
                    // sw.append("&nbsp;&nbsp;<span style=\"color: #0033CC\">"); // NOI18N
                    sb.append("&nbsp;&nbsp;<a name=").append(threadInstance.getInstanceId()).append("></a><b ").append(style).append(">");   // NOI18N
                    // -------------------------------------------------------------------
                    sb.append("\"").append(HeapUtils.htmlize(threadName)).append("\"").append(daemon.booleanValue() ? " daemon" : "").append(" prio=").append(priority);   // NOI18N
                    if (threadId != null) {
                        sb.append(" tid=").append(threadId);    // NOI18N
                    }
                    if (threadStatus != null) {
                        Thread.State tState = toThreadState(threadStatus.intValue());
                        sb.append(" ").append(tState);          // NOI18N
                    }
                    // --- Use this to enable VisualVM color scheme for threads dumps: ---
                    // sw.append("</span><br>"); // NOI18N
                    sb.append("</b><br>");   // NOI18N
                    // -------------------------------------------------------------------
                    if(stack != null) {
                        for(int i = 0; i < stack.length; i++) {
                            String stackElHref;
                            StackTraceElement stackElement = stack[i];
                            String stackElementText = HeapUtils.htmlize(stackElement.toString());

                            if (gotoSourceAvailable) {
                                String className = stackElement.getClassName();
                                String method = stackElement.getMethodName();
                                int lineNo = stackElement.getLineNumber();
                                String stackUrl = OPEN_THREADS_URL+className+"|"+method+"|"+lineNo; // NOI18N

                                // --- Use this to enable VisualVM color scheme for threads dumps: ---
                                // stackElHref = "&nbsp;&nbsp;<a style=\"color: #CC3300;\" href=\""+stackUrl+"\">"+stackElement+"</a>"; // NOI18N
                                stackElHref = "<a href=\""+stackUrl+"\">"+stackElementText+"</a>";    // NOI18N
                                // -------------------------------------------------------------------
                            } else {
                                stackElHref = stackElementText;
                            }
                            sb.append("    at ").append(stackElHref).append("<br>");  // NOI18N
                            if (localsMap != null) {
                                List<JavaFrameGCRoot> locals = localsMap.get(Integer.valueOf(i));

                                if (locals != null) {
                                    for (JavaFrameGCRoot localVar : locals) {
                                        Instance localInstance = localVar.getInstance();

                                        if (localInstance != null) {
                                            sb.append("       <span style=\"color: #666666\">local variable:</span> ").append(HeapUtils.instanceToHtml(localInstance, false, h, javaClassClass)).append("<br>"); // NOI18N
                                        } else {
                                            sb.append("      <span style=\"color: #666666\"> unknown local variable</span><br>"); // NOI18N                                                
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    sb.append("&nbsp;&nbsp;Unknown thread<br>"); // NOI18N
                }
                sb.append("<br>");  // NOI18N
            }
        }
        sb.append("</pre>"); // NOI18N
        
//        System.err.println(">>> JAVA Threads computed in " + (System.currentTimeMillis() - start));
        
        return sb.toString();
    }

    private static String getThreadName(final Heap heap, final Instance threadInstance) {
        Object threadName = threadInstance.getValueOfField("name");  // NOI18N
        
        if (threadName == null) {
            return "*null*"; // NOI18N
        }
        return DetailsSupport.getDetailsString((Instance) threadName, heap);
    }


    private static Map<ThreadObjectGCRoot,Map<Integer,List<JavaFrameGCRoot>>> computeJavaFrameMap(Collection<GCRoot> roots) {
        Map<ThreadObjectGCRoot,Map<Integer,List<JavaFrameGCRoot>>> javaFrameMap = new HashMap();
        
        for (GCRoot root : roots) {
            if (GCRoot.JAVA_FRAME.equals(root.getKind())) {
                JavaFrameGCRoot frameGCroot = (JavaFrameGCRoot) root;
                ThreadObjectGCRoot threadObj = frameGCroot.getThreadGCRoot();
                Integer frameNo = Integer.valueOf(frameGCroot.getFrameNumber());
                Map<Integer,List<JavaFrameGCRoot>> stackMap = javaFrameMap.get(threadObj);
                List<JavaFrameGCRoot> locals;
                
                if (stackMap == null) {
                    stackMap = new HashMap();
                    javaFrameMap.put(threadObj,stackMap);
                }
                locals = stackMap.get(frameNo);
                if (locals == null) {
                    locals = new ArrayList(2);
                    stackMap.put(frameNo,locals);
                }
                locals.add(frameGCroot);
            }
        }
        return javaFrameMap;
    }

//    @NbBundle.Messages({
//        "JavaThreadsHTML_FORMAT_hms={0} hrs {1} min {2} sec",
//        "JavaThreadsHTML_FORMAT_ms={0} min {1} sec"
//    })
//    private static String getTime(long millis) {
//        // Hours
//        long hours = millis / 3600000;
//        String sHours = (hours == 0 ? "" : "" + hours); // NOI18N
//        millis = millis % 3600000;
//
//        // Minutes
//        long minutes = millis / 60000;
//        String sMinutes = (((hours > 0) && (minutes < 10)) ? "0" + minutes : "" + minutes); // NOI18N
//        millis = millis % 60000;
//
//        // Seconds
//        long seconds = millis / 1000;
//        String sSeconds = ((seconds < 10) ? "0" + seconds : "" + seconds); // NOI18N
//
//        if (sHours.length() == 0) {
//            return Bundle.JavaThreadsHTML_FORMAT_ms(sMinutes, sSeconds);
//        } else {
//            return Bundle.JavaThreadsHTML_FORMAT_hms(sHours, sMinutes, sSeconds);
//        }
//    }

    /** taken from sun.misc.VM
     * 
     * Returns Thread.State for the given threadStatus
     */
    private static Thread.State toThreadState(int threadStatus) {
        if ((threadStatus & JVMTI_THREAD_STATE_RUNNABLE) != 0) {
            return Thread.State.RUNNABLE;
        } else if ((threadStatus & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER) != 0) {
            return Thread.State.BLOCKED;
        } else if ((threadStatus & JVMTI_THREAD_STATE_WAITING_INDEFINITELY) != 0) {
            return Thread.State.WAITING;
        } else if ((threadStatus & JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT) != 0) {
            return Thread.State.TIMED_WAITING;
        } else if ((threadStatus & JVMTI_THREAD_STATE_TERMINATED) != 0) {
            return Thread.State.TERMINATED;
        } else if ((threadStatus & JVMTI_THREAD_STATE_ALIVE) == 0) {
            return Thread.State.NEW;
        } else {
            return Thread.State.RUNNABLE;
        }
    }

    /* The threadStatus field is set by the VM at state transition
     * in the hotspot implementation. Its value is set according to
     * the JVM TI specification GetThreadState function.
     */
    private final static int JVMTI_THREAD_STATE_ALIVE = 0x0001;
    private final static int JVMTI_THREAD_STATE_TERMINATED = 0x0002;
    private final static int JVMTI_THREAD_STATE_RUNNABLE = 0x0004;
    private final static int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 0x0400;
    private final static int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 0x0010;
    private final static int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 0x0020;
    
}
