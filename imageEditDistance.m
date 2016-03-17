%max_image = 427;
max_image = 427;
start = 0;
edit_distances = zeros(max_image-start, 1);
block_size = 8;
for i = start:(max_image-1)
    j = i + 1
    index = i - start + 1;
    img1 = imread(strcat('STATE', num2str(i), '.png'));
    img2 = imread(strcat('STATE', num2str(j), '.png'));
    img1red = img1(:,:,1);
    [width,height] = size(img1red);
    img2red = img2(:,:,1);
    u_limit = (width/block_size) -1;
    v_limit = (height/block_size) -1;
    for u = 0:u_limit
       for v = 0:v_limit
           bx = 1 + block_size*u;
           by = 1 + block_size*v;
           block = img1red(bx:bx+(block_size-1), by:by+(block_size-1));
           match = block_size * block_size;
           for x = 1:block_size:(width-block_size)
              for y = 1:block_size:(height-block_size)
                  block2 = img2red(x:x+(block_size-1), y:y+(block_size-1));
                  block_difference = block ~= block2;
                  differences = sum(sum(block_difference));
                  if differences < match
                      match = differences;
                  end
                  
                  if match == 0
                     break; 
                  end
              end
              if match == 0
                 break;
              end
           end
           edit_distances(index) = edit_distances(index) + match;
       end
    end
end
figure()
plot((1+start):max_image, edit_distances)
    