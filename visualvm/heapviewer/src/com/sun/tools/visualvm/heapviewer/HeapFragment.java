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

package com.sun.tools.visualvm.heapviewer;

import java.io.File;
import java.io.IOException;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapProgress;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class HeapFragment {
    
    protected final String id;
    protected final String name;
    protected final String description;

    protected final Heap heap;


    public HeapFragment(String id, String name, String description, Heap heap) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.heap = heap;
    }


    public String getID() { return id; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public Heap getHeap() { return heap; }


    public static void setProgress(final ProgressHandle pHandle, final int offset) {
        final BoundedRangeModel progress = HeapProgress.getProgress();
        progress.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pHandle.progress(progress.getValue() + offset);
            }
        });
    }
    
    
    public static abstract class Provider {
    
        public abstract HeapFragment getFragment(File heapDumpFile, Lookup.Provider heapDumpProject, Heap heap) throws IOException;

    }
    
}
