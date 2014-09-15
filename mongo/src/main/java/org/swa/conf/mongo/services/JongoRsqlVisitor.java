package org.swa.conf.mongo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

class JongoRsqlVisitor implements RSQLVisitor<StringBuilder, Void> {

	private static final Map<ComparisonOperator, String> OPERATORS = new HashMap<ComparisonOperator, String>() {
		{
			put(RSQLOperators.EQUAL, "$eq");
			put(RSQLOperators.NOT_EQUAL, "$ne");
			put(RSQLOperators.GREATER_THAN_OR_EQUAL, "$gte");
			put(RSQLOperators.GREATER_THAN, "$gt");
			put(RSQLOperators.LESS_THAN_OR_EQUAL, "$lte");
			put(RSQLOperators.LESS_THAN, "$lt");
			put(RSQLOperators.IN, "$in");
			put(RSQLOperators.NOT_IN, "$nin");
		}
	};
	private static final Pattern JOKER_PATTERN = Pattern.compile("\\*");

	private final StringBuilder sb;

	JongoRsqlVisitor() {
		sb = new StringBuilder(128);
	}

	@Override
	public StringBuilder visit(final AndNode nodes, final Void x) {
		return joinChildrenNodes(nodes, "$and");
	}

	@Override
	public StringBuilder visit(final OrNode nodes, final Void x) {
		return joinChildrenNodes(nodes, "$or");
	}

	@Override
	public StringBuilder visit(final ComparisonNode node, final Void x) {
		return createCriteria(node);
	}

	private StringBuilder createCriteria(final ComparisonNode node) {

		sb.append("{'").append(node.getSelector()).append("':");

		final ComparisonOperator o = node.getOperator();
		final String a = addApostrophes(node.getArguments().get(0));

		if (RSQLOperators.EQUAL.equals(o)) {

			if (a.contains("*"))
				sb.append("{$regex:").append(JOKER_PATTERN.matcher(a).replaceAll(".*")).append(",$options:'i'}}");
			else
				sb.append(a).append("}");
		} else {

			sb.append("{").append(OPERATORS.get(node.getOperator())).append(":");

			if (node.getArguments().size() > 1) {

				sb.append("[");

				for (final String s : node.getArguments()) sb.append(addApostrophes(s)).append(",");

				// remove last comma
				sb.deleteCharAt(sb.length() - 1);

				sb.append("]");

			} else
				sb.append(a);

			sb.append("}");
		}

		return sb;
	}

	private String addApostrophes(final String s) {

		if (s.charAt(0) != '\'') return "'" + s + "'";

		return s;
	}

	private StringBuilder joinChildrenNodes(final LogicalNode node, final String op) {

		sb.append("{").append(op).append(":[");

		for (final Node child : node) child.accept(this).append(",");

		// remove last comma
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]}");

		return sb;
	}
}