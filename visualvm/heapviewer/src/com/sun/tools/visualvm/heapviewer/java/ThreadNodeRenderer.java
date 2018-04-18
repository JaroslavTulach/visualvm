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

package com.sun.tools.visualvm.heapviewer.java;

import java.awt.Font;
import javax.swing.Icon;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import com.sun.tools.visualvm.heapviewer.model.DataType;
import com.sun.tools.visualvm.heapviewer.model.HeapViewerNode;
import com.sun.tools.visualvm.heapviewer.ui.HeapViewerRenderer;

/**
 *
 * @author Jiri Sedlacek
 */
public class ThreadNodeRenderer extends LabelRenderer implements HeapViewerRenderer {
    
    private static final Icon ICON = Icons.getIcon(ProfilerIcons.THREAD);
    
    protected final Heap heap;

    
    public ThreadNodeRenderer(Heap heap) {
        this.heap = heap;
        
        setIcon(ICON);
        setFont(getFont().deriveFont(Font.BOLD));
    }
    
    
    public void setValue(Object value, int row) {
        HeapViewerNode node = (HeapViewerNode)value;
        setText(HeapViewerNode.getValue(node, DataType.NAME, heap));
    }
    
    public String getShortName() {
        String name = getText();
        int nameIdx = name.indexOf('"') + 1; // NOI18N
        if (nameIdx > 0) name = name.substring(nameIdx, name.indexOf('"', nameIdx)); // NOI18N
        return name;
    }
    
}
