import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.ArrayList;


public class Demo3D {

	public static void main(String[] args) 
	{
		JFrame f = new JFrame();
		Container pane = f.getContentPane();
		pane.setLayout(new BorderLayout());
		
		//slider to control horizontal rotation (hS)
		JSlider hS = new JSlider(0,360,180);
		pane.add(hS, BorderLayout.SOUTH);
		
		//slider to control the vertical rotation (vS)
		JSlider vS = new JSlider(SwingConstants.VERTICAL,-90,90,0);//sets slider vertical
		pane.add(vS, BorderLayout.EAST);
		
		//create a list of triangles
		List<Triangle> tris = new ArrayList<>();
		tris.add(new Triangle(new Vertex(100,100,100),
							  new Vertex(-100,-100,100),									  
							  new Vertex(-100,100,-100),
							  Color.WHITE));
		tris.add(new Triangle(new Vertex(100, 100, 100),
						         new Vertex(-100, -100, 100),
						         new Vertex(100, -100, -100),
						         Color.RED));
		tris.add(new Triangle(new Vertex(-100, 100, -100),
					         new Vertex(100, -100, -100),
					          new Vertex(100, 100, 100),
					          Color.GREEN));
		tris.add(new Triangle(new Vertex(-100, 100, -100),
			           		  new Vertex(100, -100, -100),
			           		  new Vertex(-100, -100, 100),
			                     Color.BLUE));
		
		//panel to display render results
		JPanel renderPanel = new JPanel() {
			public void paintComponent(Graphics g) 
			{
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, getWidth(), getHeight());
				
				//add triangles to screen
				g2.translate(getWidth() / 2, getHeight() / 2);
				g2.setColor(Color.WHITE);
				for(Triangle t : tris) 
				{
					Path2D path = new Path2D.Double();
					path.moveTo(t.v1.x, t.v1.y);
					path.lineTo(t.v2.x, t.v2.y);
					path.lineTo(t.v3.x, t.v3.y);
					path.closePath();
					g2.draw(path);
				}
			}
		};
		
		pane.add(renderPanel, BorderLayout.CENTER);//add the renderPanel to the main JFrame
		
		f.setSize(600,600);//set size of JFrame
		f.setVisible(true);
	}
	
	
	/**
	 * class to get the x y and z values for a triangle's vertex
	 */
	static class Vertex
	{
		double x;//left-right plane
		double y;//up-down plane
		double z;//depth plane
		Vertex(double x, double y, double z)//constructor
		{
			this.x = x;this.y = y;this.z = z;
		}
	}
	
	
	/**
	 *class to get the vertex points for the triangle, also a color 
	 */
	static class Triangle
	{
		Vertex v1;
		Vertex v2;
		Vertex v3;
		Color color;
		Triangle(Vertex v1, Vertex v2,Vertex v3, Color color)//constructor
		{
			this.v1 = v1;this.v2 = v2;this.v3 = v3;this.color = color;
		}
	}
	
	
	/**
	 * This class uses matrix multiplication to create new matrices for the
	 * XY Rotation
	 * YZ Rotation
	 * XZ Rotation
	 */
	static class Matrix3
	{
		double[] values;
		Matrix3(double[] values)//constructor
		{
			this.values = values;
		}
		Matrix3 multipy(Matrix3 other) 
		{
			double[] result = new double[9];
			for(int row = 0; row<3; row++)
			{
				for(int col =0; col<3; col++)
				{
					for(int i = 0; i<3; i++)
					{
						result[row * 3 + col] +=
								this.values[row * 3 + i] * other.values[i * 3 + col];
					}
				}
			}
			return new Matrix3(result);
		}
		Vertex transform(Vertex in)
		{
			return new Vertex(
					in.x * values[0] + in.y * values[3] + in.z * values[6],
					in.x * values[1] + in.y * values[4] + in.z * values[7],
			        in.x * values[2] + in.y * values[5] + in.z * values[8]
			        		);
		}
	}
}


