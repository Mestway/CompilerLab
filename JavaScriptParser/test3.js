function add(x1,y1,z1) {
	var c;
	var d = y1;
	z1 = z1 + "haha";

	function p1(uip) {	
		function p2(pp) {
			return pp + 1;
		}
		return p2(uip) + 1;	
	}
	c = p1(x1);	

	if(x1 < 0)
		return y1 * 3;
	else return c + d;
}
var x = 10;
var z;
z = add(x, "da", "d");