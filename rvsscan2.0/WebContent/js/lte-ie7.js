/* Load this script using conditional IE comments if you need to support IE 7 and IE 6. */

window.onload = function() {
	function addIcon(el, entity) {
		var html = el.innerHTML;
		el.innerHTML = '<span style="font-family: \'icomoon\'">' + entity + '</span>' + html;
	}
	var icons = {
			'icon-film' : '&#xe000;',
			'icon-map' : '&#xe001;',
			'icon-office' : '&#xe002;',
			'icon-earth' : '&#xe003;',
			'icon-location' : '&#xe004;',
			'icon-play' : '&#xe005;',
			'icon-pause' : '&#xe006;',
			'icon-stop' : '&#xe007;',
			'icon-x-altx-alt' : '&#xe008;',
			'icon-eject' : '&#xe009;'
		},
		els = document.getElementsByTagName('*'),
		i, attr, html, c, el;
	for (i = 0; ; i += 1) {
		el = els[i];
		if(!el) {
			break;
		}
		attr = el.getAttribute('data-icon');
		if (attr) {
			addIcon(el, attr);
		}
		c = el.className;
		c = c.match(/icon-[^\s'"]+/);
		if (c && icons[c[0]]) {
			addIcon(el, icons[c[0]]);
		}
	}
};