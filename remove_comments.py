import sys, re, os

def remove_comments(text):
    def replacer(match):
        s = match.group(0)
        if s.startswith('/'):
            return ""
        else:
            return s
    pattern = re.compile(
        r'//.*?$|/\*.*?\*/|\'(?:\\.|[^\\\'])*\'|"(?:\\.|[^\\"])*"',
        re.DOTALL | re.MULTILINE
    )
    return re.sub(pattern, replacer, text)

src_dir = 'src'
for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            clean_content = remove_comments(content)
            
            # Remove lines that only contain whitespace, then remove consecutive blank lines
            lines = clean_content.split('\n')
            new_lines = []
            for line in lines:
                if line.strip() == '':
                    # Only add a blank line if the previous wasn't blank
                    if new_lines and new_lines[-1] != '':
                        new_lines.append('')
                else:
                    new_lines.append(line)
                    
            with open(path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(new_lines) + '\n')
