var x = 10;
var t = "Str";
var b = true;
var y = 10 + 3 * x;
var k;
var kk2 = 326 + "str";

t = y * 13 - 599;

function add(x,y) {
	if(x < 0)
		return y;
	else return x + y;
}

if(x < 0)
	x = 5;
else 
	x = 3;

for(var i = 0; i < 3; i ++)
{
	if(i < 2)
  		x = x + i;
	else {
		x = x + 19 + y;
		x = x * i + add(x,y);
	}
}

var z;
z = add(x, y);
