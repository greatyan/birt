
package org.eclipse.birt.report.engine.internal.executor.l18n;

import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.engine.executor.IReportExecutor;
import org.eclipse.birt.report.engine.executor.IReportItemExecutor;
import org.eclipse.birt.report.engine.ir.MasterPageDesign;
import org.eclipse.birt.report.engine.presentation.LocalizedContentVisitor;

public class LocalizedReportExecutor implements IReportExecutor
{

	IReportExecutor executor;
	LocalizedContentVisitor l18nVisitor;
	LocalizedReportItemExecutorManager manager;

	public LocalizedReportExecutor( ExecutionContext context,
			IReportExecutor executor )
	{
		this.l18nVisitor = new LocalizedContentVisitor( context );
		this.manager = new LocalizedReportItemExecutorManager( l18nVisitor );
		this.executor = executor;
	}

	public void close( )
	{
		executor.close( );
	}

	public IReportContent execute( )
	{
		return executor.execute( );
	}

	public IReportItemExecutor getNextChild( )
	{
		IReportItemExecutor childExecutor = executor.getNextChild( );
		if ( childExecutor != null )
		{
			return manager.createExecutor( childExecutor );
		}
		return null;
	}

	public boolean hasNextChild( )
	{
		return executor.hasNextChild( );
	}

	public IReportItemExecutor createPageExecutor( long pageNumber,
			MasterPageDesign pageDesign )
	{
		IReportItemExecutor pageExecutor = executor.createPageExecutor(
				pageNumber, pageDesign );
		if ( pageExecutor != null )
		{
			return manager.createExecutor( pageExecutor );
		}
		return null;
	}
}
