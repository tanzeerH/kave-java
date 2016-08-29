package exec.recommender_reimplementation.heinemann_analysis;

import static exec.recommender_reimplementation.pbn.PBNAnalysisTestFixture.stringType;
import static exec.recommender_reimplementation.pbn.PBNAnalysisTestFixture.voidType;
import static org.junit.Assert.assertThat;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.typeshapes.TypeShape;

import com.google.common.collect.Sets;

import exec.recommender_reimplementation.pbn.PBNAnalysisBaseTest;
import exec.recommender_reimplementation.tokenization.TokenizationContext;
import exec.recommender_reimplementation.tokenization.TokenizationSettings;

public class HeinemannTokenizationVisitorTest extends PBNAnalysisBaseTest {

	private HeinemannTokenizationVisitor uut;
	private TokenizationContext tokenizationContext;
	
	@Before
	public void setup() {
		uut = new HeinemannTokenizationVisitor(new TypeShape(), 5, false);
		TokenizationSettings settings = new TokenizationSettings();
		settings.setActiveBrackets(false);
		settings.setActiveKeywords(true);
		settings.setActiveOperators(false);
		settings.setActiveWrapKeywords(true);
		settings.setActivePuncutuation(false);
		
		tokenizationContext = new TokenizationContext(settings);
	}
			
	@Test
	public void onlyIdentifiersInSameMethodBody() {
		ISST sst = sst(DefaultClassContext, 
			methodDecl(DefaultMethodContext, true, 
					varDecl("someVar", stringType),
					assign("someVar", constant("..."))),
			methodDecl(method(voidType, DefaultClassContext, "m2"), true,
					varDecl("bar", stringType),
					varDecl("errorMessage",stringType),
					assign("errorMessage", referenceExpr(varRef("bar"))),
					invokeStmt("foo", DefaultMethodContext)));		
					
		sst.accept(uut, tokenizationContext);
		
		assertThat(uut.getIndex(),Matchers.containsInAnyOrder(new Entry(Sets.newHashSet("string", "error","messag","bar"), DefaultMethodContext)));
	}
		
	@Test
	public void emtpyTokenWhenNotEnoughIdentifiers() {
		IMethodDeclaration methodDecl = methodDecl(method(voidType, DefaultClassContext, "m2"), true,
				invokeStmt("foo", DefaultMethodContext));
		
		methodDecl.accept(uut, tokenizationContext);
		
		assertThat(uut.getIndex(), Matchers.containsInAnyOrder(new Entry(Sets.newHashSet(ExtractionUtil.EMPTY_TOKEN), DefaultMethodContext)));
	}

	@Test
	public void ignoresMethodHeader() {
		uut = new HeinemannTokenizationVisitor(new TypeShape(), 2, false);
		
		IMethodDeclaration methodDecl = methodDecl(method(voidType, DefaultClassContext, "m2"), true,
				invokeStmt("foo", DefaultMethodContext));
		
		methodDecl.accept(uut, tokenizationContext);
		
		assertThat(uut.getIndex(), Matchers.containsInAnyOrder(new Entry(Sets.newHashSet(ExtractionUtil.EMPTY_TOKEN), DefaultMethodContext)));
	}

}