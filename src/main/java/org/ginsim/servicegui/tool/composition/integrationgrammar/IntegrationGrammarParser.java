// $ANTLR 3.5 /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g 2013-02-18 13:59:08

//package org.ginsim.servicegui.tool.composition.integrationgrammar.output;
//import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification;
package org.ginsim.servicegui.tool.composition.integrationgrammar;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class IntegrationGrammarParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "ENUMBER", "ID", "OR", 
		"'('", "')'", "','"
	};
	public static final int EOF=-1;
	public static final int T__8=8;
	public static final int T__9=9;
	public static final int T__10=10;
	public static final int AND=4;
	public static final int ENUMBER=5;
	public static final int ID=6;
	public static final int OR=7;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public IntegrationGrammarParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public IntegrationGrammarParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return IntegrationGrammarParser.tokenNames; }
	@Override public String getGrammarFileName() { return "/Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g"; }



	// $ANTLR start "eval"
	// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:11:1: eval returns [IntegrationFunctionSpecification.IntegrationExpression value] : exp= expression ;
	public final IntegrationFunctionSpecification.IntegrationExpression eval() throws RecognitionException {
		IntegrationFunctionSpecification.IntegrationExpression value = null;


		IntegrationFunctionSpecification.IntegrationExpression exp =null;

		try {
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:12:2: (exp= expression )
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:12:4: exp= expression
			{
			pushFollow(FOLLOW_expression_in_eval25);
			exp=expression();
			state._fsp--;

			 value = exp; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "eval"



	// $ANTLR start "expression"
	// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:15:1: expression returns [IntegrationFunctionSpecification.IntegrationExpression value] : or= disjunction ;
	public final IntegrationFunctionSpecification.IntegrationExpression expression() throws RecognitionException {
		IntegrationFunctionSpecification.IntegrationExpression value = null;


		IntegrationFunctionSpecification.IntegrationExpression or =null;

		try {
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:16:2: (or= disjunction )
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:16:4: or= disjunction
			{
			pushFollow(FOLLOW_disjunction_in_expression46);
			or=disjunction();
			state._fsp--;

			 value = or; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "expression"



	// $ANTLR start "disjunction"
	// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:19:1: disjunction returns [IntegrationFunctionSpecification.IntegrationExpression value] : c1= conjunction ( OR c2= conjunction )* ;
	public final IntegrationFunctionSpecification.IntegrationExpression disjunction() throws RecognitionException {
		IntegrationFunctionSpecification.IntegrationExpression value = null;


		IntegrationFunctionSpecification.IntegrationExpression c1 =null;
		IntegrationFunctionSpecification.IntegrationExpression c2 =null;

		try {
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:20:2: (c1= conjunction ( OR c2= conjunction )* )
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:20:4: c1= conjunction ( OR c2= conjunction )*
			{
			pushFollow(FOLLOW_conjunction_in_disjunction66);
			c1=conjunction();
			state._fsp--;

			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:20:19: ( OR c2= conjunction )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==OR) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:20:20: OR c2= conjunction
					{
					match(input,OR,FOLLOW_OR_in_disjunction69); 
					pushFollow(FOLLOW_conjunction_in_disjunction73);
					c2=conjunction();
					state._fsp--;

					}
					break;

				default :
					break loop1;
				}
			}

			value = IntegrationFunctionSpecification.createDisjunction(c1,c2); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "disjunction"



	// $ANTLR start "conjunction"
	// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:23:1: conjunction returns [IntegrationFunctionSpecification.IntegrationExpression value ] : a1= atom ( AND a2= atom )* ;
	public final IntegrationFunctionSpecification.IntegrationExpression conjunction() throws RecognitionException {
		IntegrationFunctionSpecification.IntegrationExpression value = null;


		IntegrationFunctionSpecification.IntegrationExpression a1 =null;
		IntegrationFunctionSpecification.IntegrationExpression a2 =null;

		try {
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:24:2: (a1= atom ( AND a2= atom )* )
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:24:4: a1= atom ( AND a2= atom )*
			{
			pushFollow(FOLLOW_atom_in_conjunction99);
			a1=atom();
			state._fsp--;

			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:24:12: ( AND a2= atom )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==AND) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:24:13: AND a2= atom
					{
					match(input,AND,FOLLOW_AND_in_conjunction102); 
					pushFollow(FOLLOW_atom_in_conjunction106);
					a2=atom();
					state._fsp--;

					}
					break;

				default :
					break loop2;
				}
			}

			value = IntegrationFunctionSpecification.createConjunction(a1,a2); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "conjunction"



	// $ANTLR start "atom"
	// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:28:1: atom returns [ IntegrationFunctionSpecification.IntegrationExpression value] : (id= ID '(' threshold= ENUMBER ',' min= ENUMBER ',' max= ENUMBER ')' |id= ID '(' threshold= ENUMBER ',' min= ENUMBER ',' max= ENUMBER ',' dist= ENUMBER ')' | '(' exp= expression ')' );
	public final IntegrationFunctionSpecification.IntegrationExpression atom() throws RecognitionException {
		IntegrationFunctionSpecification.IntegrationExpression value = null;


		Token id=null;
		Token threshold=null;
		Token min=null;
		Token max=null;
		Token dist=null;
		IntegrationFunctionSpecification.IntegrationExpression exp =null;

		try {
			// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:29:2: (id= ID '(' threshold= ENUMBER ',' min= ENUMBER ',' max= ENUMBER ')' |id= ID '(' threshold= ENUMBER ',' min= ENUMBER ',' max= ENUMBER ',' dist= ENUMBER ')' | '(' exp= expression ')' )
			int alt3=3;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==ID) ) {
				int LA3_1 = input.LA(2);
				if ( (LA3_1==8) ) {
					int LA3_3 = input.LA(3);
					if ( (LA3_3==ENUMBER) ) {
						int LA3_4 = input.LA(4);
						if ( (LA3_4==10) ) {
							int LA3_5 = input.LA(5);
							if ( (LA3_5==ENUMBER) ) {
								int LA3_6 = input.LA(6);
								if ( (LA3_6==10) ) {
									int LA3_7 = input.LA(7);
									if ( (LA3_7==ENUMBER) ) {
										int LA3_8 = input.LA(8);
										if ( (LA3_8==9) ) {
											alt3=1;
										}
										else if ( (LA3_8==10) ) {
											alt3=2;
										}

										else {
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 8 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 3, 8, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}

									else {
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 3, 7, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 3, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 3, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 3, 4, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 3, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 3, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA3_0==8) ) {
				alt3=3;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:29:4: id= ID '(' threshold= ENUMBER ',' min= ENUMBER ',' max= ENUMBER ')'
					{
					id=(Token)match(input,ID,FOLLOW_ID_in_atom132); 
					match(input,8,FOLLOW_8_in_atom134); 
					threshold=(Token)match(input,ENUMBER,FOLLOW_ENUMBER_in_atom138); 
					match(input,10,FOLLOW_10_in_atom140); 
					min=(Token)match(input,ENUMBER,FOLLOW_ENUMBER_in_atom144); 
					match(input,10,FOLLOW_10_in_atom146); 
					max=(Token)match(input,ENUMBER,FOLLOW_ENUMBER_in_atom150); 
					match(input,9,FOLLOW_9_in_atom152); 
					 value = IntegrationFunctionSpecification.createAtom((id!=null?id.getText():null),(threshold!=null?threshold.getText():null),(max!=null?max.getText():null),(min!=null?min.getText():null));
					}
					break;
				case 2 :
					// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:31:4: id= ID '(' threshold= ENUMBER ',' min= ENUMBER ',' max= ENUMBER ',' dist= ENUMBER ')'
					{
					id=(Token)match(input,ID,FOLLOW_ID_in_atom163); 
					match(input,8,FOLLOW_8_in_atom165); 
					threshold=(Token)match(input,ENUMBER,FOLLOW_ENUMBER_in_atom169); 
					match(input,10,FOLLOW_10_in_atom171); 
					min=(Token)match(input,ENUMBER,FOLLOW_ENUMBER_in_atom175); 
					match(input,10,FOLLOW_10_in_atom177); 
					max=(Token)match(input,ENUMBER,FOLLOW_ENUMBER_in_atom181); 
					match(input,10,FOLLOW_10_in_atom183); 
					dist=(Token)match(input,ENUMBER,FOLLOW_ENUMBER_in_atom187); 
					match(input,9,FOLLOW_9_in_atom189); 
					 value = IntegrationFunctionSpecification.createAtom((id!=null?id.getText():null),(threshold!=null?threshold.getText():null),(min!=null?min.getText():null),(max!=null?max.getText():null),(dist!=null?dist.getText():null));
					}
					break;
				case 3 :
					// /Users/nuno/projects/nmd/ginsim-dev/src/main/java/org/ginsim/servicegui/tool/composition/IntegrationGrammar.g:33:3: '(' exp= expression ')'
					{
					match(input,8,FOLLOW_8_in_atom196); 
					pushFollow(FOLLOW_expression_in_atom200);
					exp=expression();
					state._fsp--;

					match(input,9,FOLLOW_9_in_atom202); 
					 value = IntegrationFunctionSpecification.createAtom(exp);
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "atom"

	// Delegated rules



	public static final BitSet FOLLOW_expression_in_eval25 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_disjunction_in_expression46 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conjunction_in_disjunction66 = new BitSet(new long[]{0x0000000000000082L});
	public static final BitSet FOLLOW_OR_in_disjunction69 = new BitSet(new long[]{0x0000000000000140L});
	public static final BitSet FOLLOW_conjunction_in_disjunction73 = new BitSet(new long[]{0x0000000000000082L});
	public static final BitSet FOLLOW_atom_in_conjunction99 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_AND_in_conjunction102 = new BitSet(new long[]{0x0000000000000140L});
	public static final BitSet FOLLOW_atom_in_conjunction106 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_ID_in_atom132 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_8_in_atom134 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ENUMBER_in_atom138 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_10_in_atom140 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ENUMBER_in_atom144 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_10_in_atom146 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ENUMBER_in_atom150 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_9_in_atom152 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_atom163 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_8_in_atom165 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ENUMBER_in_atom169 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_10_in_atom171 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ENUMBER_in_atom175 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_10_in_atom177 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ENUMBER_in_atom181 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_10_in_atom183 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ENUMBER_in_atom187 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_9_in_atom189 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_8_in_atom196 = new BitSet(new long[]{0x0000000000000140L});
	public static final BitSet FOLLOW_expression_in_atom200 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_9_in_atom202 = new BitSet(new long[]{0x0000000000000002L});
}
