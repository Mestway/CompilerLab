var x = 10;
var t = "Str";
var b = true;
var y = 10 + 3 * x;
var k;
var kk2 = 326 + "str";

t++;

t = y * 3 - 2;
t--;

var i = 0;

i++;

while(i < x)
{
	y = y + 1;
	kk2 = true;
	i ++;
}

function add(x1,y1,z1) {
	var c;
	var d = y1;

	z1 = z1 + "haha";

	c = x1;	

	if(x1 < 0)
		return y1 * 3;
	else return c + d;
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
		x = x * i + add(x,y,4);
	}
}

var z;
z = add(x, "da", "d");
