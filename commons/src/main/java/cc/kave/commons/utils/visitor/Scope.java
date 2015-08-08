package cc.kave.commons.utils.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.references.IVariableReference;

public class Scope {
	public Scope parent;
	public Set<IVariableReference> existingIds;
	public Map<IVariableReference, IVariableReference> changedNames;
	public List<IStatement> body;

	public Scope() {
		this.parent = null;
		this.existingIds = new HashSet<>();
		this.changedNames = new HashMap<>();
		this.body = new ArrayList<>();
	}

	public IVariableReference resolve(IVariableReference ref) {
		if (changedNames.containsKey(ref)) {
			return changedNames.get(ref);
		} else
			return ref;
	}
}
