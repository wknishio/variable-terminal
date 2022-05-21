/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 * 
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple layout manager the puts all components on a single line, either horizontally or vertically.
 */
public class LinearLayout implements LayoutManager {
    /**
     * This enum type will decide the alignment of a component on the counter-axis, meaning the horizontal alignment on
     * vertical {@code LinearLayout}s and vertical alignment on horizontal {@code LinearLayout}s.
     */
    public enum Alignment {
        /**
         * The component will be placed to the left (for vertical layouts) or top (for horizontal layouts)
         */
        Beginning,
        /**
         * The component will be placed horizontally centered (for vertical layouts) or vertically centered (for
         * horizontal layouts)
         */
        Center,
        /**
         * The component will be placed to the right (for vertical layouts) or bottom (for horizontal layouts)
         */
        End,
        /**
         * The component will be forced to take up all the horizontal space (for vertical layouts) or vertical space
         * (for horizontal layouts)
         */
        Fill,
    }

    /**
     * This enum type will what to do with a component if the container has extra space to offer. This can happen if the
     * window runs in full screen or the window has been programmatically set to a fixed size, above the preferred size
     * of the window.
     */
    public enum GrowPolicy {
        /**
         * This is the default grow policy, the component will not become larger than the preferred size, even if the
         * container can offer more.
         */
        None,
        /**
         * With this grow policy, if the container has more space available then this component will be grown to fill
         * the extra space.
         */
        CanGrow,
    }

    private static class LinearLayoutData implements LayoutData {
        private final Alignment alignment;
        private final GrowPolicy growPolicy;

        public LinearLayoutData(Alignment alignment, GrowPolicy growPolicy) {
            this.alignment = alignment;
            this.growPolicy = growPolicy;
        }
    }

    /**
     * Creates a {@code LayoutData} for {@code LinearLayout} that assigns a component to a particular alignment on its
     * counter-axis, meaning the horizontal alignment on vertical {@code LinearLayout}s and vertical alignment on
     * horizontal {@code LinearLayout}s.
     * @param alignment Alignment to store in the {@code LayoutData} object
     * @return {@code LayoutData} object created for {@code LinearLayout}s with the specified alignment
     * @see Alignment
     */
    public static LayoutData createLayoutData(Alignment alignment) {
        return createLayoutData(alignment, GrowPolicy.None);
    }

    /**
     * Creates a {@code LayoutData} for {@code LinearLayout} that assigns a component to a particular alignment on its
     * counter-axis, meaning the horizontal alignment on vertical {@code LinearLayout}s and vertical alignment on
     * horizontal {@code LinearLayout}s.
     * @param alignment Alignment to store in the {@code LayoutData} object
     * @param growPolicy When policy to apply to the component if the parent container has more space available along
     *                   the main axis.
     * @return {@code LayoutData} object created for {@code LinearLayout}s with the specified alignment
     * @see Alignment
     */
    public static LayoutData createLayoutData(Alignment alignment, GrowPolicy growPolicy) {
        return new LinearLayoutData(alignment, growPolicy);
    }

    private final Direction direction;
    private int spacing;
    private boolean changed;

    /**
     * Default constructor, creates a vertical {@code LinearLayout}
     */
    public LinearLayout() {
        this(Direction.VERTICAL);
    }

    /**
     * Standard constructor that creates a {@code LinearLayout} with a specified direction to position the components on
     * @param direction Direction for this {@code Direction}
     */
    public LinearLayout(Direction direction) {
        this.direction = direction;
        this.spacing = direction == Direction.HORIZONTAL ? 1 : 0;
        this.changed = true;
    }

    /**
     * Sets the amount of empty space to put in between components. For horizontal layouts, this is number of columns
     * (by default 1) and for vertical layouts this is number of rows (by default 0).
     * @param spacing Spacing between components, either in number of columns or rows depending on the direction
     * @return Itself
     */
    public LinearLayout setSpacing(int spacing) {
        this.spacing = spacing;
        this.changed = true;
        return this;
    }

    /**
     * Returns the amount of empty space to put in between components. For horizontal layouts, this is number of columns
     * (by default 1) and for vertical layouts this is number of rows (by default 0).
     * @return Spacing between components, either in number of columns or rows depending on the direction
     */
    public int getSpacing() {
        return spacing;
    }

    
    public TerminalSize getPreferredSize(List<Component> components) {
        //components = components.stream().filter(Component::isVisible).collect(Collectors.toList());
    	List<Component> visibles = new ArrayList<Component>();
    	for (Component component : components)
    	{
    		if (component.isVisible())
    		{
    			visibles.add(component);
    		}
    	}
        if(direction == Direction.VERTICAL) {
            return getPreferredSizeVertically(visibles);
        }
        else {
            return getPreferredSizeHorizontally(visibles);
        }
    }

    private TerminalSize getPreferredSizeVertically(List<Component> components) {
    	
        int maxWidth = 0;
        int height = 0;
        for(Component component: components) {
            TerminalSize preferredSize = component.getPreferredSize();
            if(maxWidth < preferredSize.getColumns()) {
                maxWidth = preferredSize.getColumns();
            }
            height += preferredSize.getRows();
        }
        height += spacing * (components.size() - 1);
        return new TerminalSize(maxWidth, Math.max(0, height));
    }

    private TerminalSize getPreferredSizeHorizontally(List<Component> components) {
        int maxHeight = 0;
        int width = 0;
        for(Component component: components) {
            TerminalSize preferredSize = component.getPreferredSize();
            if(maxHeight < preferredSize.getRows()) {
                maxHeight = preferredSize.getRows();
            }
            width += preferredSize.getColumns();
        }
        width += spacing * (components.size() - 1);
        return new TerminalSize(Math.max(0,width), maxHeight);
    }

    
    public boolean hasChanged() {
        return changed;
    }

    
    public void doLayout(TerminalSize area, List<Component> components) {
        List<Component> visibles = new ArrayList<Component>();
        for (Component component : components)
    	{
    		if (component.isVisible())
    		{
    			visibles.add(component);
    		}
    	}
        if(direction == Direction.VERTICAL) {
            if (Boolean.getBoolean("com.googlecode.lanterna.gui2.LinearLayout.useOldNonFlexLayout")) {
                doVerticalLayout(area, visibles);
            }
            else {
                doFlexibleVerticalLayout(area, visibles);
            }
        }
        else {
            if (Boolean.getBoolean("com.googlecode.lanterna.gui2.LinearLayout.useOldNonFlexLayout")) {
                doHorizontalLayout(area, visibles);
            }
            else {
                doFlexibleHorizontalLayout(area, visibles);
            }
        }
        this.changed = false;
    }

    @Deprecated
    private void doVerticalLayout(TerminalSize area, List<Component> components) {
        int remainingVerticalSpace = area.getRows();
        int availableHorizontalSpace = area.getColumns();
        for(Component component: components) {
            if(remainingVerticalSpace <= 0) {
                component.setPosition(TerminalPosition.TOP_LEFT_CORNER);
                component.setSize(TerminalSize.ZERO);
            }
            else {
                Alignment alignment = Alignment.Beginning;
                LayoutData layoutData = component.getLayoutData();
                if (layoutData instanceof LinearLayoutData) {
                    alignment = ((LinearLayoutData)layoutData).alignment;
                }

                TerminalSize preferredSize = component.getPreferredSize();
                TerminalSize decidedSize = new TerminalSize(
                        Math.min(availableHorizontalSpace, preferredSize.getColumns()),
                        Math.min(remainingVerticalSpace, preferredSize.getRows()));
                if(alignment == Alignment.Fill) {
                    decidedSize = decidedSize.withColumns(availableHorizontalSpace);
                    alignment = Alignment.Beginning;
                }

                TerminalPosition position = component.getPosition();
                position = position.withRow(area.getRows() - remainingVerticalSpace);
                switch(alignment) {
                    case End:
                        position = position.withColumn(availableHorizontalSpace - decidedSize.getColumns());
                        break;
                    case Center:
                        position = position.withColumn((availableHorizontalSpace - decidedSize.getColumns()) / 2);
                        break;
                    case Beginning:
                    default:
                        position = position.withColumn(0);
                        break;
                }
                component.setPosition(position);
                component.setSize(component.getSize().with(decidedSize));
                remainingVerticalSpace -= decidedSize.getRows() + spacing;
            }
        }
    }

    private void doFlexibleVerticalLayout(TerminalSize area, List<Component> components) {
        int availableVerticalSpace = area.getRows();
        int availableHorizontalSpace = area.getColumns();
        List<Component> copyOfComponents = new ArrayList<Component>(components);
        final Map<Component, TerminalSize> fittingMap = new IdentityHashMap<Component, TerminalSize>();
        int totalRequiredVerticalSpace = 0;

        for (Component component: components) {
            Alignment alignment = Alignment.Beginning;
            LayoutData layoutData = component.getLayoutData();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData)layoutData).alignment;
            }

            TerminalSize preferredSize = component.getPreferredSize();
            TerminalSize fittingSize = new TerminalSize(
                    Math.min(availableHorizontalSpace, preferredSize.getColumns()),
                    preferredSize.getRows());
            if(alignment == Alignment.Fill) {
                fittingSize = fittingSize.withColumns(availableHorizontalSpace);
            }

            fittingMap.put(component, fittingSize);
            totalRequiredVerticalSpace += fittingSize.getRows() + spacing;
        }
        if (!components.isEmpty()) {
            // Remove the last spacing
            totalRequiredVerticalSpace -= spacing;
        }

        // If we can't fit everything, trim the down the size of the largest components until it fits
        if (availableVerticalSpace < totalRequiredVerticalSpace) {
        	
        	Collections.sort(copyOfComponents, new Comparator<Component>() {
                
                public int compare(Component o1, Component o2) {
                    // Reverse sort
                    return 0 - new Integer(fittingMap.get(o1).getRows()).compareTo(fittingMap.get(o2).getRows());
                }
            });

            while (availableVerticalSpace < totalRequiredVerticalSpace) {
                int largestSize = fittingMap.get(copyOfComponents.get(0)).getRows();
                for (Component largeComponent: copyOfComponents) {
                    TerminalSize currentSize = fittingMap.get(largeComponent);
                    if (largestSize > currentSize.getRows()) {
                        break;
                    }
                    fittingMap.put(largeComponent, currentSize.withRelativeRows(-1));
                    totalRequiredVerticalSpace--;
                }
            }
        }

        // If we have more space available than we need, grow components to fill
        if (availableVerticalSpace > totalRequiredVerticalSpace) {
            boolean resizedOneComponent = false;
            while (availableVerticalSpace > totalRequiredVerticalSpace) {
                for(Component component: components) {
                    final LinearLayoutData layoutData = (LinearLayoutData)component.getLayoutData();
                    final TerminalSize currentSize = fittingMap.get(component);
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CanGrow) {
                        fittingMap.put(component, currentSize.withRelativeRows(1));
                        availableVerticalSpace--;
                        resizedOneComponent = true;
                    }
                    if (availableVerticalSpace <= totalRequiredVerticalSpace) {
                        break;
                    }
                }
                if (!resizedOneComponent) {
                    break;
                }
            }
        }

        // Assign the sizes and positions
        int topPosition = 0;
        for(Component component: components) {
            Alignment alignment = Alignment.Beginning;
            LayoutData layoutData = component.getLayoutData();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData)layoutData).alignment;
            }

            TerminalSize decidedSize = fittingMap.get(component);
            TerminalPosition position = component.getPosition();
            position = position.withRow(topPosition);
            switch(alignment) {
                case End:
                    position = position.withColumn(availableHorizontalSpace - decidedSize.getColumns());
                    break;
                case Center:
                    position = position.withColumn((availableHorizontalSpace - decidedSize.getColumns()) / 2);
                    break;
                case Beginning:
                default:
                    position = position.withColumn(0);
                    break;
            }
            component.setPosition(component.getPosition().with(position));
            component.setSize(component.getSize().with(decidedSize));
            topPosition += decidedSize.getRows() + spacing;
        }
    }

    @Deprecated
    private void doHorizontalLayout(TerminalSize area, List<Component> components) {
        int remainingHorizontalSpace = area.getColumns();
        int availableVerticalSpace = area.getRows();
        for(Component component: components) {
            if(remainingHorizontalSpace <= 0) {
                component.setPosition(TerminalPosition.TOP_LEFT_CORNER);
                component.setSize(TerminalSize.ZERO);
            }
            else {
                Alignment alignment = Alignment.Beginning;
                LayoutData layoutData = component.getLayoutData();
                if (layoutData instanceof LinearLayoutData) {
                    alignment = ((LinearLayoutData)layoutData).alignment;
                }

                TerminalSize preferredSize = component.getPreferredSize();
                TerminalSize decidedSize = new TerminalSize(
                        Math.min(remainingHorizontalSpace, preferredSize.getColumns()),
                        Math.min(availableVerticalSpace, preferredSize.getRows()));
                if(alignment == Alignment.Fill) {
                    decidedSize = decidedSize.withRows(availableVerticalSpace);
                    alignment = Alignment.Beginning;
                }

                TerminalPosition position = component.getPosition();
                position = position.withColumn(area.getColumns() - remainingHorizontalSpace);
                switch(alignment) {
                    case End:
                        position = position.withRow(availableVerticalSpace - decidedSize.getRows());
                        break;
                    case Center:
                        position = position.withRow((availableVerticalSpace - decidedSize.getRows()) / 2);
                        break;
                    case Beginning:
                    default:
                        position = position.withRow(0);
                        break;
                }
                component.setPosition(position);
                component.setSize(component.getSize().with(decidedSize));
                remainingHorizontalSpace -= decidedSize.getColumns() + spacing;
            }
        }
    }

    private void doFlexibleHorizontalLayout(TerminalSize area, List<Component> components) {
        int availableVerticalSpace = area.getRows();
        int availableHorizontalSpace = area.getColumns();
        List<Component> copyOfComponents = new ArrayList<Component>(components);
        final Map<Component, TerminalSize> fittingMap = new IdentityHashMap<Component, TerminalSize>();
        int totalRequiredHorizontalSpace = 0;

        for (Component component: components) {
            Alignment alignment = Alignment.Beginning;
            LayoutData layoutData = component.getLayoutData();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData)layoutData).alignment;
            }

            TerminalSize preferredSize = component.getPreferredSize();
            TerminalSize fittingSize = new TerminalSize(
                    preferredSize.getColumns(),
                    Math.min(availableVerticalSpace, preferredSize.getRows()));
            if(alignment == Alignment.Fill) {
                fittingSize = fittingSize.withRows(availableVerticalSpace);
            }

            fittingMap.put(component, fittingSize);
            totalRequiredHorizontalSpace += fittingSize.getColumns() + spacing;
        }
        if (!components.isEmpty()) {
            // Remove the last spacing
            totalRequiredHorizontalSpace -= spacing;
        }

        // If we can't fit everything, trim the down the size of the largest components until it fits
        if (availableHorizontalSpace < totalRequiredHorizontalSpace) {
        		Collections.sort(copyOfComponents, new Comparator<Component>() {
                
                public int compare(Component o1, Component o2) {
                    // Reverse sort
                    return 0 - new Integer(fittingMap.get(o1).getColumns()).compareTo(fittingMap.get(o2).getColumns());
                }
            });

            while (availableHorizontalSpace < totalRequiredHorizontalSpace) {
                int largestSize = fittingMap.get(copyOfComponents.get(0)).getColumns();
                for (Component largeComponent: copyOfComponents) {
                    TerminalSize currentSize = fittingMap.get(largeComponent);
                    if (largestSize > currentSize.getColumns()) {
                        break;
                    }
                    fittingMap.put(largeComponent, currentSize.withRelativeColumns(-1));
                    totalRequiredHorizontalSpace--;
                }
            }
        }

        // If we have more space available than we need, grow components to fill
        if (availableHorizontalSpace > totalRequiredHorizontalSpace) {
            boolean resizedOneComponent = false;
            while (availableHorizontalSpace > totalRequiredHorizontalSpace) {
                for(Component component: components) {
                    final LinearLayoutData layoutData = (LinearLayoutData)component.getLayoutData();
                    final TerminalSize currentSize = fittingMap.get(component);
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CanGrow) {
                        fittingMap.put(component, currentSize.withRelativeColumns(1));
                        availableHorizontalSpace--;
                        resizedOneComponent = true;
                    }
                    if (availableHorizontalSpace <= totalRequiredHorizontalSpace) {
                        break;
                    }
                }
                if (!resizedOneComponent) {
                    break;
                }
            }
        }

        // Assign the sizes and positions
        int leftPosition = 0;
        for(Component component: components) {
            Alignment alignment = Alignment.Beginning;
            LayoutData layoutData = component.getLayoutData();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData)layoutData).alignment;
            }

            TerminalSize decidedSize = fittingMap.get(component);
            TerminalPosition position = component.getPosition();
            position = position.withColumn(leftPosition);
            switch(alignment) {
                case End:
                    position = position.withRow(availableVerticalSpace - decidedSize.getRows());
                    break;
                case Center:
                    position = position.withRow((availableVerticalSpace - decidedSize.getRows()) / 2);
                    break;
                case Beginning:
                default:
                    position = position.withRow(0);
                    break;
            }
            component.setPosition(component.getPosition().with(position));
            component.setSize(component.getSize().with(decidedSize));
            leftPosition += decidedSize.getColumns() + spacing;
        }
    }
}
