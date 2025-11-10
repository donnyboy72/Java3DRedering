import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
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
				
				//set up horzinotal slider to work
				double horizontal = Math.toRadians(hS.getValue());
				Matrix3 horizTransform = new Matrix3(new double[] 
						{
						Math.cos(horizontal),0,-Math.sin(horizontal),
						0,1,0,
						Math.sin(horizontal),0,Math.cos(horizontal)
						});
				
				//set up vertical slider to work
				double vertical = Math.toRadians(vS.getValue());
				Matrix3 vertTransform = new Matrix3(new double[]
						{ 
						1,0,0,
						0,Math.cos(vertical),Math.sin(vertical),
						0,-Math.sin(vertical),Math.cos(vertical)
						});
				
				//call multiply transform method
				Matrix3 transform = horizTransform.multiply(vertTransform);
				
				//add triangles to screen
				BufferedImage img = 
					    new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
				
				// initialize z-buffer (same size as the image)
				double[] zBuffer = new double[img.getWidth() * img.getHeight()];
				for (int q = 0; q < zBuffer.length; q++) {
				    zBuffer[q] = Double.NEGATIVE_INFINITY;
				}
				
				for(Triangle t : tris) 
				{	
					Vertex v1 = transform.transform(t.v1);
					Vertex v2 = transform.transform(t.v2);
					Vertex v3 = transform.transform(t.v3);
					v1.x += getWidth() / 2;
				    v1.y += getHeight() / 2;
				    v2.x += getWidth() / 2;
				    v2.y += getHeight() / 2;
				    v3.x += getWidth() / 2;
				    v3.y += getHeight() / 2;
				
				    
				 // compute rectangular bounds for triangle
				    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
				    int maxX = (int) Math.min(img.getWidth() - 1, 
				                              Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
				    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
				    int maxY = (int) Math.min(img.getHeight() - 1,
				                              Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));
				    
				    double triangleArea =
				    	       (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

				    	    for (int y = minY; y <= maxY; y++) {
				    	        for (int x = minX; x <= maxX; x++) {
				    	            double b1 = 
				    	              ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
				    	            double b2 =
				    	              ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
				    	            double b3 =
				    	              ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
				    	            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
				    	                // interpolate depth using barycentric coordinates
				    	                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;

				    	                // index into the zBuffer
				    	                int zIndex = y * img.getWidth() + x;

				    	                // draw only if this pixel is closer
				    	                if (zBuffer[zIndex] < depth) {
				    	                    img.setRGB(x, y, t.color.getRGB());
				    	                    zBuffer[zIndex] = depth;
				    	                }
				    	            }
				    	        }
				    	        
				    	    }
				    	    
				    g2.drawImage(img,0,0,null);
				}
			}
		};
		
		//add listeners for horiznotal slider and vertical slider
		hS.addChangeListener(e-> renderPanel.repaint());
		vS.addChangeListener(e-> renderPanel.repaint());
		
		pane.add(renderPanel, BorderLayout.CENTER);//add the renderPanel to the main JFrame
		
		f.setSize(1200,600);//set size of JFrame
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
		Matrix3 multiply(Matrix3 other)//multiplies one matrix by another 
		{
			double[] result = new double[9];//create an new array
			
			for(int row = 0; row<3; row++)//breaks orginal list into 3 rows 
			{
				for(int col =0; col<3; col++)//breaks orginal list into 3 columns
				{
					for(int i = 0; i<3; i++)
					/*does dot product between one row and one colm of second list
					//row * 3 + col makes a 1D index from a 2D array
					//thi.svalue and this.other value get correct elements from
					///maxtrices
					 */
					{
						result[row * 3 + col] +=
								this.values[row * 3 + i] * other.values[i * 3 + col];
					}
				}
			}
			return new Matrix3(result);//returns new matrix
		}
		
		Vertex transform(Vertex in)//method to transform a vertex
		{
			return new Vertex
					(
					in.x * values[0] + in.y * values[3] + in.z * values[6],
					in.x * values[1] + in.y * values[4] + in.z * values[7],
			        in.x * values[2] + in.y * values[5] + in.z * values[8]
			        );
		}
	}
}


