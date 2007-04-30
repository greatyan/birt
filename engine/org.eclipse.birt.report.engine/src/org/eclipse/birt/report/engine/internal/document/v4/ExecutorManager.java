/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.internal.document.v4;

import java.util.LinkedList;

import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.engine.internal.document.PageHintReader;
import org.eclipse.birt.report.engine.internal.document.v3.CachedReportContentReaderV3;
import org.eclipse.birt.report.engine.ir.AutoTextItemDesign;
import org.eclipse.birt.report.engine.ir.CellDesign;
import org.eclipse.birt.report.engine.ir.DataItemDesign;
import org.eclipse.birt.report.engine.ir.DefaultReportItemVisitorImpl;
import org.eclipse.birt.report.engine.ir.ExtendedItemDesign;
import org.eclipse.birt.report.engine.ir.FreeFormItemDesign;
import org.eclipse.birt.report.engine.ir.GridItemDesign;
import org.eclipse.birt.report.engine.ir.ImageItemDesign;
import org.eclipse.birt.report.engine.ir.LabelItemDesign;
import org.eclipse.birt.report.engine.ir.ListBandDesign;
import org.eclipse.birt.report.engine.ir.ListGroupDesign;
import org.eclipse.birt.report.engine.ir.ListItemDesign;
import org.eclipse.birt.report.engine.ir.MultiLineItemDesign;
import org.eclipse.birt.report.engine.ir.ReportItemDesign;
import org.eclipse.birt.report.engine.ir.RowDesign;
import org.eclipse.birt.report.engine.ir.TableBandDesign;
import org.eclipse.birt.report.engine.ir.TableGroupDesign;
import org.eclipse.birt.report.engine.ir.TableItemDesign;
import org.eclipse.birt.report.engine.ir.TemplateDesign;
import org.eclipse.birt.report.engine.ir.TextItemDesign;

/**
 * 
 * report item executor manager
 * 
 */
public class ExecutorManager
{

	/**
	 * item executor type
	 */
	public static final int GRIDITEM = 0;
	public static final int IMAGEITEM = 1;
	public static final int LABELITEM = 2;
	public static final int LISTITEM = 3;
	public static final int TABLEITEM = 4;
	public static final int MULTILINEITEM = 5;
	public static final int TEXTITEM = 6;
	public static final int DATAITEM = 7;
	public static final int EXTENDEDITEM = 8;
	public static final int TEMPLATEITEM = 9;
	public static final int AUTOTEXTITEM = 10;
	public static final int LISTBANDITEM = 11;
	public static final int TABLEBANDITEM = 12;
	public static final int ROWITEM = 13;
	public static final int CELLITEM = 14;
	public static final int LISTGROUPITEM = 15;
	public static final int TABLEGROUPITEM = 16;

	/**
	 * the number of suppported executor
	 */
	public static final int NUMBER = 17;

	/**
	 * factory used to create the report executor
	 */
	protected ExecutorFactory executorFactory;
	/**
	 * array of free list
	 */
	protected LinkedList[] freeList = new LinkedList[NUMBER];

	AbstractReportExecutor reportExecutor;

	/**
	 * constructor
	 * 
	 * @param context
	 * @param visitor
	 */
	public ExecutorManager( AbstractReportExecutor reportExecutor )
	{
		this.reportExecutor = reportExecutor;
		for ( int i = 0; i < NUMBER; i++ )
		{
			freeList[i] = new LinkedList( );
		}
		executorFactory = new ExecutorFactory( );
	}

	ExecutionContext getExecutionContext( )
	{
		return reportExecutor.context;
	}

	long generateUniqueID( )
	{
		return reportExecutor.uniqueId++;
	}

	void setUniqueID( long id )
	{
		reportExecutor.uniqueId = id;
	}

	CachedReportContentReaderV3 getReportReader( )
	{
		return reportExecutor.reader;
	}

	CachedReportContentReaderV3 getPageReader( )
	{
		return reportExecutor.pageReader;
	}

	PageHintReader getPageHintReader( )
	{
		return reportExecutor.hintsReader;
	}

	/**
	 * get item executor
	 * 
	 * @param type
	 *            the executor type
	 * @return item executor
	 */
	protected ReportItemExecutor getItemExecutor( int type )
	{
		assert ( type >= 0 ) && ( type < NUMBER );
		if ( !freeList[type].isEmpty( ) )
		{
			// the free list is non-empty
			return (ReportItemExecutor) freeList[type].removeFirst( );
		}
		switch ( type )
		{
			case GRIDITEM :
				return new GridItemExecutor( this );
			case IMAGEITEM :
				return new ImageItemExecutor( this );
			case LABELITEM :
				return new LabelItemExecutor( this );
			case LISTITEM :
				return new ListItemExecutor( this );
			case TABLEITEM :
				return new TableItemExecutor( this );
			case MULTILINEITEM :
				return new MultiLineItemExecutor( this );
			case TEXTITEM :
				return new TextItemExecutor( this );
			case DATAITEM :
				return new DataItemExecutor( this );
			case EXTENDEDITEM :
				return new ExtendedItemExecutor( this );
			case TEMPLATEITEM :
				return new TemplateExecutor( this );
			case AUTOTEXTITEM :
				return new AutoTextItemExecutor( this );
			case LISTBANDITEM :
				return new ListBandExecutor( this );
			case TABLEBANDITEM :
				return new TableBandExecutor( this );
			case ROWITEM :
				return new RowExecutor( this );
			case CELLITEM :
				return new CellExecutor( this );
			case LISTGROUPITEM :
				return new ListGroupExecutor( this );
			case TABLEGROUPITEM :
				return new TableGroupExecutor( this );
			default :
				throw new UnsupportedOperationException(
						"unsupported executor!" ); //$NON-NLS-1$
		}
	}

	public ReportItemExecutor createExecutor( ReportItemExecutor parent,
			ReportItemDesign design, long offset )
	{
		ReportItemExecutor executor = executorFactory.createExecutor( design );
		if ( executor != null )
		{
			executor.setParent( parent );
			executor.setDesign( design );
			executor.setOffset( offset );
		}
		return executor;
	}

	/**
	 * release item executor
	 * 
	 * @param type
	 *            the executor type
	 * @param itemExecutor
	 *            the item executor
	 */
	public void releaseExecutor( ReportItemExecutor itemExecutor )
	{
		int type = itemExecutor.getExecutorType( );
		if ( type >= 0 && type < NUMBER )
		{
			freeList[type].add( itemExecutor );
		}
	}

	class ExecutorFactory extends DefaultReportItemVisitorImpl
	{

		public ReportItemExecutor createExecutor( ReportItemDesign design )
		{
			return (ReportItemExecutor) design.accept( this, null );
		}

		public Object visitAutoTextItem( AutoTextItemDesign autoText,
				Object value )
		{
			return getItemExecutor( AUTOTEXTITEM );
		}

		public Object visitCell( CellDesign cell, Object value )
		{
			return getItemExecutor( CELLITEM );
		}

		public Object visitDataItem( DataItemDesign data, Object value )
		{
			return getItemExecutor( DATAITEM );
		}

		public Object visitExtendedItem( ExtendedItemDesign item, Object value )
		{
			return getItemExecutor( EXTENDEDITEM );
		}

		public Object visitFreeFormItem( FreeFormItemDesign container,
				Object value )
		{
			return null;
		}

		public Object visitGridItem( GridItemDesign grid, Object value )
		{
			return getItemExecutor( GRIDITEM );
		}

		public Object visitImageItem( ImageItemDesign image, Object value )
		{
			return getItemExecutor( IMAGEITEM );
		}

		public Object visitLabelItem( LabelItemDesign label, Object value )
		{
			return getItemExecutor( LABELITEM );
		}

		public Object visitListBand( ListBandDesign band, Object value )
		{
			return getItemExecutor( LISTBANDITEM );
		}

		public Object visitListItem( ListItemDesign list, Object value )
		{
			return getItemExecutor( LISTITEM );
		}

		public Object visitMultiLineItem( MultiLineItemDesign multiLine,
				Object value )
		{
			return getItemExecutor( MULTILINEITEM );
		}

		public Object visitRow( RowDesign row, Object value )
		{
			return getItemExecutor( ROWITEM );
		}

		public Object visitTableBand( TableBandDesign band, Object value )
		{
			return getItemExecutor( TABLEBANDITEM );
		}

		public Object visitTableItem( TableItemDesign table, Object value )
		{
			return getItemExecutor( TABLEITEM );
		}

		public Object visitTemplate( TemplateDesign template, Object value )
		{
			return getItemExecutor( TEMPLATEITEM );
		}

		public Object visitTextItem( TextItemDesign text, Object value )
		{
			return getItemExecutor( TEXTITEM );
		}

		public Object visitListGroup( ListGroupDesign group, Object value )
		{
			return getItemExecutor( LISTGROUPITEM );
		}

		public Object visitTableGroup( TableGroupDesign group, Object value )
		{
			return getItemExecutor( TABLEGROUPITEM );
		}

	}

}
