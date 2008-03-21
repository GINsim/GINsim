function getElementsByContent(content, tagName) {
	var elements = new Array();
	var nodes = document.getElementsByTagName(tagName);
	for (var i = 0, node; node = nodes[i]; i++) {
			if (node.innerHTML.indexOf(content) != -1) elements.push(node);
	}
	return elements;
}

function nextSiblingOfType(node, nodeName) {
	node = node.nextSibling;
	while (node.nodeName != nodeName && node.nextSibling != null) {
		node = node.nextSibling;
	}
	return node;
}

function makeStableStatesClickable() {
	var tables = getElementsByContent('Stable states', 'td');
	var i;
	for (i = 0 ; i - tables.length != 0 ; i++) {
		var td_stableState = tables[i];//the td containing Stable states
		var tr = tables[0].parentNode;
		p = document.createElement('p');
		p.innerHTML = 'Stable States';
		a = document.createElement('a');
		a.setAttribute('href', 'javascript:toggle("stableState'+i+'")');
		a.innerHTML = ' (View)';
		p.appendChild(a);
		tables[i].replaceChild(p, tables[i].firstChild) ;

		tr = nextSiblingOfType(tr, tr.nodeName)
		var td_new = nextSiblingOfType(tr.firstChild, tables[i].nodeName);
		td = document.createElement('td');
		td.setAttribute('colspan', '5');
		p = document.createElement('p');
		p.innerHTML = td_new.innerHTML;
		p.setAttribute('id', 'stableState'+i);
		p.style.display = 'none';
		td.appendChild(p);
		tr.replaceChild(td, td_new);
	}
}

function toggle(id) {
	elm = document.getElementById(id);
	if (elm.style.display == 'none') {
		elm.style.display = 'inline'
	} else {
		elm.style.display = 'none'
	}
}

window.onload = makeStableStatesClickable;